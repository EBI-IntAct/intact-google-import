/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.cvedit.struts.view;

import uk.ac.ebi.intact.model.*;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;

/**
 * Bean to display an Intact object. This bean is used by edit.jsp to display
 * and capture the data.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class CvViewBean {

    /**
     * Static empty container to return to JSPs to display
     * no rows (to stop display tag library throwing an exception).
     */
    private static Collection theirEmptyCollection = Collections.EMPTY_LIST;

    /**
     * Selected topic.
     */
    private String mySelectedTopic;

    /**
     * Accession number.
     */
    private String myAc;

    /**
     * The short label.
     */
    private String myShortLabel;

    /**
     * The annotations to display.
     */
    private Collection myAnnotations = new ArrayList();

    /**
     * The Xreferences to display.
     */
    private Collection myXrefs = new ArrayList();

    /**
     * Holds annotations to add. This collection is cleared once the user
     * commits the transaction.
     */
    private transient Collection myAnnotsToAdd = new ArrayList();

    /**
     * Holds annotations to del. This collection is cleared once the user
     * commits the transaction.
     */
    private transient Collection myAnnotsToDel = new ArrayList();

    /**
     * Holds xrefs to add. This collection is cleared once the user commits
     * the transaction.
     */
    private transient Collection myXrefsToAdd = new ArrayList();

    /**
     * Holds xrefs to del. This collection is cleared once the user commits
     * the transaction.
     */
    private transient Collection myXrefsToDel = new ArrayList();

    /**
     * Set attributes using values from CvObject. A coarse-grained method to
     * avoid multiple method calls.
     *
     * @param cvobj the <code>CvObject</code> to set attributes of this class.
     */
    public void initialise(CvObject cvobj) {
        setAc(cvobj.getAc());
        setShortLabel(cvobj.getShortLabel());

        // Cache the annotations and xrefs here to save it from loading
        // multiple times with each invocation to getAnnotations()
        // or getXrefs() methods.
        myAnnotations.clear();
        makeCommentBeans(cvobj.getAnnotation());
        myXrefs.clear();
        makeXrefBeans(cvobj.getXref());
    }

    /**
     * Sets the selected topic.
      * @param topic the selected topic.
     */
    public void setTopic(String topic) {
        mySelectedTopic = topic;
    }

    /**
     * Returns the selected topic.
     */
    public String getTopic() {
        return mySelectedTopic;
    }

    /**
     * Sets the accession number.
     * @param ac the accession number; shouldn't be null.
     */
    public void setAc(String ac) {
        myAc = ac;
    }

    /**
     * Returns accession number.
     */
    public String getAc() {
        return myAc;
    }

    /**
     * Sets ther short label.
     * @param shortLabel the short label to set
     */
    public void setShortLabel(String shortLabel) {
        myShortLabel = shortLabel;
    }

    /**
     * Returns the short label.
     */
    public String getShortLabel() {
        return myShortLabel;
    }

    /**
     * Returns a collection of <code>CommentBean</code> objects.
     *
     * <pre>
     * post: return != null
     * post: return->forall(obj : Object | obj.oclIsTypeOf(CommentBean))
     * </pre>
     */
    public Collection getAnnotations() {
        return myAnnotations;
    }

    /**
     * Adds an annotation.
     * @param annotation the annotation to add.
     *
     * <pre>
     * post: myAnnotsToAdd = myAnnotsToAdd@pre + 1
     * post: myAnnotations = myAnnotations@pre + 1
     * </pre>
     */
    public void addAnnotation(Annotation annotation) {
        CommentBean cb = new CommentBean(annotation);
        // Annotation to add.
        myAnnotsToAdd.add(cb);
        // Add to the view as well.
        myAnnotations.add(cb);
    }

    /**
     * Removes an annotation
     * @param cb the comment bean to remove.
     *
     * <pre>
     * post: myAnnotsToDel = myAnnotsToDel@pre - 1
     * post: myAnnotations = myAnnotations@pre - 1
     * </pre>
     */
    public void delAnnotation(CommentBean cb) {
        // Add to the container to delete annotations.
        myAnnotsToDel.add(cb);
        // Remove from the view as well.
        myAnnotations.remove(cb);
    }

    /**
     * Finds the bean for a given key.
     * @param key the key to search for the bean.
     * @return the <code>CommentBean</code> object whose key matches with given
     * <code>key</code>. <code>null</code> is returned if no matching bean
     * is found.
     */
    public CommentBean findAnnotationCB(long key) {
        for (Iterator iter = myAnnotations.iterator(); iter.hasNext();) {
            CommentBean bean = (CommentBean) iter.next();
            if (bean.getKey() == key) {
                return bean;
            }
        }
        // Not found the bean.
        return null;
    }

    /**
     * Returns a collection of annotations to add.
     * @return the collection of annotations to add to the current CV object.
     * Could be empty if there are no annotations to add.
     *
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(CommentBean)
     * </pre>
     */
    public Collection getAnnotationsToAdd() {
        // Annotations common to both add and delete.
        Collection common = CollectionUtils.intersection(myAnnotsToAdd, myAnnotsToDel);
        // All the annotations only found in annotations to add collection.
        return CollectionUtils.subtract(myAnnotsToAdd, common);
    }

    /**
     * Returns a collection of annotations to remove.
     * @return the collection of annotations to remove from the current CV object.
     * Could be empty if there are no annotations to delete.
     *
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(CommentBean)
     * </pre>
     */
    public Collection getAnnotationsToDel() {
        // Annotations common to both add and delete.
        Collection common = CollectionUtils.intersection(myAnnotsToAdd, myAnnotsToDel);
        // All the annotations only found in annotations to delete collection.
        return CollectionUtils.subtract(myAnnotsToDel, common);
    }

    /**
     * Clears annotations stored transaction containers.
     *
     * <pre>
     * post: myAnnotsToAdd.isEmpty()
     * post: myAnnotsToDel.isEmpty()
     * </pre>
     */
    public void clearTransAnnotations() {
        myAnnotsToAdd.clear();
        myAnnotsToDel.clear();
    }

    /**
     * Returns a collection <code>Xref</code> objects.
     *
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(Xref))
     * </pre>
     */
    public Collection getXrefs() {
        return myXrefs;
    }

    /**
     * Adds an xref.
     * @param xref the xref to add.
     *
     * <pre>
     * post: myXrefsToAdd = myXrefsToAdd@pre + 1
     * post: myXrefs = myXrefs@pre + 1
     * </pre>
     */
    public void addXref(Xref xref) {
        XreferenceBean xb = new XreferenceBean(xref);
        // Xref to add.
        myXrefsToAdd.add(xb);
        // Add to the view as well.
        myXrefs.add(xb);
    }

    /**
     * Removes a xref.
     * @param xb the X'reference bean to remove.
     *
     * <pre>
     * post: myXrefsToDel = myXrefsToDel@pre + 1
     * post: myXrefs = myXrefs@pre - 1
     * </pre>
     */
    public void delXref(XreferenceBean xb) {
        // Add to the container to delete the xref.
        myXrefsToDel.add(xb);
        // Remove from the view as well.
        myXrefs.remove(xb);
    }

    /**
     * Finds the bean for a given key.
     * @param key the key to search for the bean.
     * @return the <code>XreferenceBean</code> object whose key matches with given
     * <code>key</code>. <code>null</code> is returned if no matching bean
     * is found.
     */
    public XreferenceBean findXref(long key) {
        for (Iterator iter = myXrefs.iterator(); iter.hasNext();) {
            XreferenceBean bean = (XreferenceBean) iter.next();
            if (bean.getKey() == key) {
                return bean;
            }
        }
        // Not found the bean.
        return null;
    }

    /**
     * Returns a collection of xrefs to add.
     * @return the collection of xrefs to add to the current CV object.
     * Could be empty if there are no xrefs to add.
     *
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(XreferenceBean))
     * </pre>
     */
    public Collection getXrefsToAdd() {
        // Xrefs common to both add and delete.
        Collection common = CollectionUtils.intersection(myXrefsToAdd, myXrefsToDel);
        // All the xrefs only found in xrefs to add collection.
        return CollectionUtils.subtract(myXrefsToAdd, common);
    }

    /**
     * Returns a collection of xrefs to remove.
     * @return the collection of xrefs to remove from the current CV object.
     * Could be empty if there are no xrefs to delete.
     *
     * <pre>
     * post: return->forall(obj: Object | obj.oclIsTypeOf(XreferenceBean))
     * </pre>
     */
    public Collection getXrefsToDel() {
        // Xrefs common to both add and delete.
        Collection common = CollectionUtils.intersection(myXrefsToAdd, myXrefsToDel);
        // All the xrefs only found in xrefs to delete collection.
        return CollectionUtils.subtract(myXrefsToDel, common);
    }

    /**
     * Clears xrefs stored in transaction containers.
     *
     * <pre>
     * post: myXrefsToAdd.isEmpty()
     * post: myXrefsToDel.isEmpty()
     * </pre>
     */
    public void clearTransXrefs() {
        myXrefsToAdd.clear();
        myXrefsToDel.clear();
    }

    /**
     * Returns an empty collection. This is to stop display tag library from
     * throwing an exception when there are no rows to display for a high page
     * number (only happens when we have two tables with different rows on
     * a single page). <b>This has to be an insatace method not static for
     * JSPs</b>.
     *
     * <pre>
     * post: return->isEmpty
     * </pre>
     */
    public Collection getEmptyCollection() {
        return theirEmptyCollection;
    }

    /**
     * Returns a string representation of this object. Mainly for debugging.
     */
    public String toString() {
        return "ac: " + getAc() + " short label: " + getShortLabel();
    }

    // Helper Methods

    /**
     * Creates a collection of <code>CommentBean</code> created from given
     * collection of annotations.
     * @param annotations a collection of <code>Annotation</code> objects.
     */
    private void makeCommentBeans(Collection annotations) {
        for (Iterator iter = annotations.iterator(); iter.hasNext();) {
            Annotation annot = (Annotation) iter.next();
            myAnnotations.add(new CommentBean(annot));
        }
    }

    /**
     * Creates a collection of <code>XrefBean</code> created from given
     * collection of xreferences.
     * @param xrefs a collection of <code>Xref</code> objects.
     */
    private void makeXrefBeans(Collection xrefs) {
        for (Iterator iter = xrefs.iterator(); iter.hasNext();) {
            Xref xref = (Xref) iter.next();
            myXrefs.add(new XreferenceBean(xref));
        }
    }
}