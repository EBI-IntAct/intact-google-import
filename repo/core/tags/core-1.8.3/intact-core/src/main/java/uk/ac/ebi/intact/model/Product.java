/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.model;

/**
 * Describes products of an interaction.
 * <p/>
 * example In a phosphorylation, this object would link to the phosphorylated Protein.
 *
 * @author hhe
 * @version $Id$
 */
public class Product extends BasicObjectImpl {

    ///////////////////////////////////////
    //attributes

    //attributes used for mapping BasicObjects - project synchron
    // TODO: should be move out of the model.
    public String interactionAc;
    public String interactorAc;
    public String cvProductRoleAc;

    /**
     * TODO Represents ...
     */
    private float stoichiometry = 1;

    ///////////////////////////////////////
    // associations

    /**
     * TODO comments
     */
    private Interaction interaction;

    /**
     * TODO comments
     */
    private Interactor interactor;

    /**
     * TODO comments
     */
    private CvProductRole cvProductRole;

    /**
     * This constructor should <b>not</b> be used as it could
     * result in objects with invalid state. It is here for object mapping
     * purposes only and if possible will be made private.
     *
     * @deprecated Use the full constructor instead (when it exists)
     */
    @Deprecated
    public Product() {
        super();
    }

    ///////////////////////////////////////
    //access methods for attributes

    public float getStoichiometry() {
        return stoichiometry;
    }

    public void setStoichiometry( float stoichiometry ) {
        this.stoichiometry = stoichiometry;
    }

    ///////////////////////////////////////
    // access methods for associations

    public Interaction getInteraction() {
        return interaction;
    }

    public void setInteraction( Interaction interaction ) {
        if ( this.interaction != interaction ) {
            if ( this.interaction != null ) this.interaction.removeReleased( this );
            this.interaction = interaction;
            if ( interaction != null ) interaction.addReleased( this );
        }
    }

    public Interactor getInteractor() {
        return interactor;
    }

    public void setInteractor( Interactor interactor ) {
        if ( this.interactor != interactor ) {
            if ( this.interactor != null ) this.interactor.removeProduct( this );
            this.interactor = interactor;
            if ( interactor != null ) interactor.addProduct( this );
        }
    }

    public CvProductRole getCvProductRole() {
        return cvProductRole;
    }

    public void setCvProductRole( CvProductRole cvProductRole ) {
        this.cvProductRole = cvProductRole;
    }


    /**
     * Equality for Product is currently based on equality for
     * stoichiometry, CvProductRole, Interaction and Interactor.
     *
     * @param o The object to check
     *
     * @return true if the parameter equlas this object, false otherwise
     *
     * @see uk.ac.ebi.intact.model.CvProductRole
     * @see uk.ac.ebi.intact.model.Interaction
     * @see uk.ac.ebi.intact.model.Interactor
     */
    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( !( o instanceof Product ) ) return false;

        final Product product = ( Product ) o;

        //TODO Needs work later to make it more readable...
        //generated by IntelliJ - needs rewriting when we use this class....
        if ( stoichiometry != product.stoichiometry ) return false;
        if ( cvProductRole != null ? !cvProductRole.equals( product.cvProductRole ) : product.cvProductRole != null )
            return false;
        if ( interaction != null ? !interaction.equals( product.interaction ) : product.interaction != null )
            return false;
        if ( interactor != null ? !interactor.equals( product.interactor ) : product.interactor != null ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;

        //generated by IntelliJ - may need rewriting when we use the class...
        result = Float.floatToIntBits( stoichiometry );
        result = 29 * result + ( interaction != null ? interaction.hashCode() : 0 );
        result = 29 * result + ( interactor != null ? interactor.hashCode() : 0 );
        result = 29 * result + ( cvProductRole != null ? cvProductRole.hashCode() : 0 );
        return result;
    }

    //attributes used for mapping BasicObjects - project synchron
    // TODO: should be move out of the model.
    public String getInteractionAc() {
        return this.interactionAc;
    }

    public void getInteractionAc( String ac ) {
        this.interactionAc = ac;
    }

    public String getInteractorAc() {
        return this.interactorAc;
    }

    public void getInteractorAc( String ac ) {
        this.interactorAc = ac;
    }

    public String getCvProductRoleAc() {
        return this.cvProductRoleAc;
    }

    public void getcvProductRoleAc( String ac ) {
        this.cvProductRoleAc = ac;
    }

} // end Product




