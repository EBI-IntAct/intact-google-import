/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.search.struts.controller;

import uk.ac.ebi.intact.application.search.struts.framework.IntactBaseAction;
import uk.ac.ebi.intact.application.search.struts.framework.util.SearchConstants;
import uk.ac.ebi.intact.application.search.struts.framework.util.TreeViewAction;
import uk.ac.ebi.intact.application.search.struts.view.IntactViewBean;
import uk.ac.ebi.intact.application.search.struts.view.ViewForm;
import uk.ac.ebi.intact.util.*;
import uk.ac.ebi.intact.business.*;
import uk.ac.ebi.intact.model.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;

import java.util.*;

//for debugging only
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.traversal.*;


/**
 * Process the user submitted search form.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk), Chris Lewington (clewing@ebi.ac.uk)
 * @version $Id$
 */
public class ViewAction extends IntactBaseAction {

   /**
    * Process the specified HTTP request, and create the corresponding
    * HTTP response (or forward to another web component that will create
    * it). Return an ActionForward instance describing where and how
    * control should be forwarded, or null if the response has
    * already been completed.
    *
    * @param mapping - The <code>ActionMapping</code> used to select this instance
    * @param form - The optional <code>ActionForm</code> bean for this request (if any)
    * @param request - The HTTP request we are processing
    * @param response - The HTTP response we are creating
    *
    * @return - represents a destination to which the controller servlet,
    * <code>ActionServlet</code>, might be directed to perform a RequestDispatcher.forward()
    * or HttpServletResponse.sendRedirect() to, as a result of processing
    * activities of an <code>Action</code> class
    */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {
        // The collection of beans to process.
        Map idToView = (Map) super.getSession(request).getAttribute(
                SearchConstants.FORWARD_MATCHES);

       //The set of objects in this session which have recently been expanded
       Set expandedSet = (Set)super.getSession(request).getAttribute(SearchConstants.EXPANDED_AC_SET);
       if (expandedSet == null) {
           //should have been set up by SearchAction, but wasn't so do it here
           super.getSession(request).setAttribute(SearchConstants.EXPANDED_AC_SET, new HashSet());
       }
        // Save the parameters from the view page.
        Map map = request.getParameterMap();

       //debug stuff...
       System.out.println("new view requested...");
       System.out.println("Check box keys submitted in request:");
       for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                if (!key.startsWith("tbl_")) {
                    continue;
                }
                System.out.println("key: " + key);
       }
       System.out.println();
       System.out.println("Keys that we know have already been expanded:");
       for(Iterator it = expandedSet.iterator(); it.hasNext();) {
           System.out.println(it.next());
       }
       System.out.println();


        // Only search for the check boxes if the number of parameters in a
        // request exceed 1 (one parameter is always returned for buttons).
        if (map.size() > 1) {

            // Search through the parameter lists (check boxes).
            for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();

                // Ignore the non tbl parameters.
                if (!key.startsWith("tbl_")) {
                    continue;
                }

                // Extract the bean id and the AC.
                StringTokenizer stk = new StringTokenizer(key, "_");
                String ignore = stk.nextToken();
                String beanId = stk.nextToken();
                String ac = stk.nextToken();

                // The view bean associated with the id.
                IntactViewBean bean = (IntactViewBean) idToView.get(beanId);

                //tell the bean to do whatever action was requested
                List acList = new ArrayList();
                acList.add(ac);

                if(((ViewForm)form).expandContractSelected()) {
                    try {

                        int mode = XmlBuilder.CONTRACT_NODES;    //default op
                        if(!expandedSet.contains(key)) {
                            expandedSet.add(key);
                            mode = XmlBuilder.EXPAND_NODES;
                            System.out.println("not yet expanded in view - adding to set..");
                        }

                        Iterator setIter = expandedSet.iterator();
                        System.out.println("expanded so far:");
                        while(setIter.hasNext()) {
                                System.out.println(setIter.next());
                        }

                        if(mode == XmlBuilder.CONTRACT_NODES) {

                            System.out.println("contract node " + ac);
                            System.out.println("....removing key " + key + " from expanded set..");
                            expandedSet.remove(key);

                            //if the root (wrapped) object is to be contracted - all
                            //the ACs below should be too, ie those in the list
                            Object wrappedObj = bean.getWrappedObject();
                            String wrappedAc = null;
                            if(wrappedObj instanceof BasicObject) wrappedAc = ((BasicObject)wrappedObj).getAc();
                            if(wrappedObj instanceof Institution) wrappedAc = ((Institution)wrappedObj).getAc();

                            String keyToMatch = "tbl_" + beanId + wrappedAc;
                            if(key.equals(keyToMatch)) {
                                //the bean ID represents the top level item, so go
                                //through the expanded set and remove those in it containing
                                //that ID
                                System.out.println("AC to contract is the search object..");
                                this.updateExpandedAcs(expandedSet, beanId);

                            }
                        }

                        //using System.out for logging as we want the bean info, which
                        //does not have access to a logger...
                        TransformerFactory tf = TransformerFactory.newInstance();
                        Transformer transformer = tf.newTransformer();
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //adds whitespace
                        System.out.println("expand/contract selected");
                        System.out.println("XML before transform..");
                        System.out.println();
                        Document doc1 = bean.getAsXmlDoc();
                        transformer.transform(new DOMSource(doc1), new StreamResult(System.out));
                        System.out.println();

                        Iterator it = acList.iterator();
                        System.out.println("ACs to be expanded/contracted:");
                        while(it.hasNext()) {
                            System.out.println((String)it.next());
                        }
                        //do it
                        bean.modifyXml(mode, acList);

                        if(mode == XmlBuilder.EXPAND_NODES) {
                            //See if the AC expansion was an Interaction - if so then need to
                            //expand its Components also, but if not don't do any more
                            //NB as we only have XML available we have to traverse it to find
                            //an AC/Interaction match...
                            Document doc2 = bean.getAsXmlDoc();
                            Node root = doc2.getDocumentElement();
                            NodeIterator nodeIter = ((DocumentTraversal)doc2).createNodeIterator(root, NodeFilter.SHOW_ALL,null,true);
                            Node n = null;
                            System.out.println("Checking for an Interaction...");
                            while((n = nodeIter.nextNode()) != null) {

                                Element e = (Element)n;
                                System.out.println("element name: " + e.getTagName());
                                System.out.println("element AC: " + e.getAttribute("ac"));
                                if((ac.equals(e.getAttribute("ac"))) &
                                    (e.getTagName().equals("Interaction"))) {
                                    System.out.println("found an Interaction/AC match..");
                                    doComponents(doc2, bean, mode);
                                }
                            }
                        }
                        System.out.println();
                        System.out.println("XML after transform..");
                        Document doc3 = bean.getAsXmlDoc();
                        transformer.transform(new DOMSource(doc3), new StreamResult(System.out));

                    }
                    catch(IntactException ie) {

                        //return with errors...
                        super.log("modify XML failed" + ie.toString());
                    }
                    catch(Exception e) {
                        //just to pick up the transformer exceptions
                    }
                }
                if(((ViewForm)form).expandAllSelected()) {
                    try {
                        TransformerFactory tf = TransformerFactory.newInstance();
                        Transformer transformer = tf.newTransformer();
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        //first expand the node itself
                        bean.modifyXml(XmlBuilder.EXPAND_NODES, acList);
                        System.out.println("XML before full expand..");
                        System.out.println();
                        Document doc1 = bean.getAsXmlDoc();
                        transformer.transform(new DOMSource(doc1), new StreamResult(System.out));
                        System.out.println();
                        //need to start at the tree Node that was selected for expand -
                        //otherwise the whole tree will be done!
                        //NB don't expand the siblings of the first Node to check,
                        //so pass empty Collection...
                        Element start = bean.getElement(ac);
                        if(start != null) {
                            System.out.println("found element with AC " + ac + " - expanding from there..");
                            this.expandTree(bean, start, new ArrayList());
                        }
                        //no-op if the AC doesn't exist for some reason in the tree
                        System.out.println();
                        System.out.println("XML after full expand..");
                        Document doc3 = bean.getAsXmlDoc();
                        transformer.transform(new DOMSource(doc3), new StreamResult(System.out));

                    }
                    catch(IntactException ie) {

                        //return with errors...
                        super.log("modify XML failed during full expansion" + ie.toString());
                    }
                    catch(Exception e) {
                        //transformer only
                    }

                }

            }
        }
//        else {
//            // No check boxes were selected. Assume all is wanted.
//            for (Iterator iter = idToView.values().iterator(); iter.hasNext(); ) {
//                IntactViewBean bean = (IntactViewBean) iter.next();
//                bean.setTreeStatus(action);
//            }
//        }
        // Move to the results page.
        return mapping.findForward(SearchConstants.FORWARD_RESULTS);
    }


    //--------------------------- private helper methods -------------------------------

    /**
     * expands/contracts Component Elements when an Interaction has been requested
     * to be modifed. This has to be done because we are interested in the Proteins
     * that a Component holds for a given Interaction, and this would not appear
     * with a first expansion.
     *
     * @param doc The XML Document to modify
     * @param bean The bean to apply the changes to
     * @param mode the mode (expand/contract)
     *
     * @exception IntactException thrown if the modification failed
     */
    private void doComponents(Document doc, IntactViewBean bean, int mode) throws IntactException {

        List componentList = new ArrayList();
        System.out.println("Interaction expansion - checking for Component elements...");
        NodeList nodes = doc.getElementsByTagName("Component");
        if((nodes != null) & (nodes.getLength() != 0)) {

            System.out.println("found Components - expanding...");
            for(int i=0; i < nodes.getLength(); i++) {
                Node compNode = nodes.item(i);
                if(compNode.hasAttributes()) {
                    //it should do - something very odd if not!!
                    Attr acAttr = (Attr)compNode.getAttributes().getNamedItem("ac"); //know ac is an attribute
                    componentList.add(acAttr.getValue());
                    System.out.println("Component AC: " + acAttr.getValue());
                }
            }
            System.out.println("calling expand for the Components...");
            bean.modifyXml(mode, componentList);

        }
    }

    /**
     * Updates the expanded Set of ACs to remove those related to the root object
     * when it needs contracting. In other words, sub-items need to be taken out
     * of the expanded set if they exist there because otherwise the next time the
     * root object is expanded, the sub-items will have the wrong mode associated with
     * them.
     *
     * @param expandedAcs The set of ACs currently expanded
     * @param id The id of the Bean we are interested in
     */
    private void updateExpandedAcs (Set expandedAcs, String id) {

        Iterator it = expandedAcs.iterator();
        System.out.println("removing expansion keys associated with bean ID = " + id +":-");
        Set toRemove = new HashSet();
        //have to copy them, *then* remove them as the Iterator
        //will get confused if some get deleted along the way
        //NB probably a List would have been better..
        while(it.hasNext()) {
                String acKey = (String)it.next();
                if(acKey.indexOf("tbl_" + id) != -1) toRemove.add(acKey);
        }
        Iterator toRemoveIter = toRemove.iterator();
        System.out.println("sub-elements to be removed from expansion set:");
        while(toRemoveIter.hasNext()) {
            Object obj = toRemoveIter.next();
            System.out.println(obj);
            expandedAcs.remove(obj);
        }
    }

    /**
     * Method to recursively expand the Elements of a tree until it can't be done
     * anymore.
     *
     * @param bean The bean whose Document is to be expanded
     * @param currentNode The first Node of the tree identifying the tree level and hence
     * @param siblings The siblings of the current Node
     * where to start expanding from.
     *
     * @exception IntactException thrown if there was a modify problem
     */
    private void expandTree(IntactViewBean bean, Node currentNode, List siblings) throws IntactException {

        //keep expanding the item until there are no children left..
        //Basic algorithm:
        //for each Node in a list of siblings:
        //get the ACs of the Node's children, then expand the bean for those ACs;
        //when the siblings are all done, move down to the next level of the DOM tree;
        //repeat the above until all Nodes at one level no longer have any children

        System.out.println("current Node: " + ((Element)currentNode).getAttribute("ac"));
        System.out.println("current Node Tag: " + ((Element)currentNode).getTagName());
        Node nextSibling = null;

        //local copy of siblings
        List remainingSibs = new ArrayList();
        if(!siblings.isEmpty()) {
            System.out.println("sibling class: " + siblings.get(0).getClass());
            Node sib = (Node)siblings.get(0);
            if(sib != null) {
                System.out.println("current Node first sibling: " + sib.getNodeType() + sib.getNodeName());
            }
            else { System.out.println("no next sibling!!"); }
            Iterator siblingIter = siblings.iterator();
            while(siblingIter.hasNext()) {
                Object obj = siblingIter.next();
                if(obj instanceof Element) {
                    Element e = (Element)obj;
                    System.out.println("sibling AC: " + e.getAttribute("ac"));
                    System.out.println("sibling Tag: " + e.getTagName());
                    remainingSibs.add(e);
                }
                else { System.out.println("sibling type wrong! Type is " + ((Node)obj).getNodeType()); }
            }
        }
        else {System.out.println("current Node has no siblings - continuing..."); }

        //base case - all Nodes at this level have no children
        if(!currentNode.hasChildNodes()) {
            boolean done = true;
            if(!siblings.isEmpty()) {
                Iterator it = siblings.iterator();
                while(it.hasNext()) {
                    Node checkNode = (Node)it.next();
                    if(checkNode.hasChildNodes()) {
                        //found one - better start here instead
                        System.out.println("sibling has children - changing target node to it..");
                        currentNode = checkNode;
                        done = false;
                        break;
                    }
                }
            }
            System.out.println("done expanding for node " + ((Element)currentNode).getAttribute("ac"));
            if(done) return;
        }
        List childAcs = new ArrayList();
        NodeList children = currentNode.getChildNodes();
        Element nextLevelChild = (Element)children.item(0);
        System.out.println("next level first Node: " + nextLevelChild.getAttribute("ac"));
        System.out.println("Children of current node " + ((Element)currentNode).getAttribute("ac") + ":");
        for(int i=0; i<children.getLength(); i++) {
            Element childElem = (Element)children.item(i);
            System.out.println("child: " + childElem.getAttribute("ac"));
            if(childElem.hasAttribute("ac")) childAcs.add(childElem.getAttribute("ac"));

            //add the children into the sibling list for the next level down
            remainingSibs.add(childElem);
        }
        //got the current Node's child ACs, so now modify the bean data
        System.out.println("modifying bean with child ACs..");
        bean.modifyXml(XmlBuilder.EXPAND_NODES, childAcs);
        //now do the siblings and move to the next level
        if(!siblings.isEmpty()) {
            System.out.println("now doing siblings...");
            Iterator sibIterator = siblings.iterator();

            while(sibIterator.hasNext()) {
                this.expandTree(bean, (Node)sibIterator.next(), siblings);
            }
        }
        else {System.out.println("no siblings to check"); }
        System.out.println("now moving down to next level...");
        //take out the next level "first" child as it is the first node to check at the
        //next level
        remainingSibs.remove(nextLevelChild);
        this.expandTree(bean, nextLevelChild, remainingSibs);
    }
}
