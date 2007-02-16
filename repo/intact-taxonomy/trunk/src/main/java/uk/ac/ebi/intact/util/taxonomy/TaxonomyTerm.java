/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.taxonomy;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Description of a taxonomy term.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
public class TaxonomyTerm implements Serializable {

    /**
     * Class version for checking when serializing.
     */
    private static final long serialVersionUID = 3886076190511081376L;

    /**
     * Set of non redundant parents.
     */
    private Set<TaxonomyTerm> parents = new HashSet<TaxonomyTerm>( 1 );

    /**
     * Set of non redundant children.
     */
    private Set<TaxonomyTerm> children = new HashSet<TaxonomyTerm>( );

    /**
     * Taxid of the newt term.
     */
    private int taxid;

    /**
     * Scientific name of the term.
     */
    private String scientificName;

    /**
     * Common name of the term.
     */
    private String commonName;

    ///////////////////
    // Constructor

    public TaxonomyTerm( int taxid ) {
        setTaxid( taxid );
    }

    ////////////////////////
    // Getters and Setters

    /**
     * Getter for property 'commonName'.
     *
     * @return Value for property 'commonName'.
     */
    public String getCommonName() {
        return commonName;
    }

    /**
     * Setter for property 'commonName'.
     *
     * @param commonName Value to set for property 'commonName'.
     */
    public void setCommonName( String commonName ) {
        this.commonName = commonName;
    }

    /**
     * Getter for property 'scientificName'.
     *
     * @return Value for property 'scientificName'.
     */
    public String getScientificName() {
        return scientificName;
    }

    /**
     * Setter for property 'scientificName'.
     *
     * @param scientificName Value to set for property 'scientificName'.
     */
    public void setScientificName( String scientificName ) {
        this.scientificName = scientificName;
    }

    /**
     * Getter for property 'taxid'.
     *
     * @return Value for property 'taxid'.
     */
    public int getTaxid() {
        return taxid;
    }

    /**
     * Setter for property 'taxid'.
     *
     * @param taxid Value to set for property 'taxid'.
     */
    public void setTaxid( int taxid ) {
        if( ! TaxonomyUtils.isSupportedTaxid( taxid ) ) {
            throw new IllegalArgumentException( taxid + ": a taxid must be > -4." );
        }
        this.taxid = taxid;
    }

    public void addChild( TaxonomyTerm child ) {
        children.add( child );
        child.parents.add( this );
    }

    public void removeChild( TaxonomyTerm child ) {
        children.remove( child );
        child.parents.remove( this );
    }

    public void addParent( TaxonomyTerm parent ) {
        parents.add( parent );
        parent.children.add( this );
    }

    public void removeParent( TaxonomyTerm parent ) {
        parents.remove( parent );
        parent.children.remove( this );
    }

    public Collection<TaxonomyTerm> getChildren() {
        return Collections.unmodifiableSet( children );
    }

    public Collection<TaxonomyTerm> getParents() {
        return Collections.unmodifiableSet( parents );
    }

    ///////////////////////
    // Object override

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append( "TaxonomyTerm" );
        sb.append( "{commonName='" ).append( commonName ).append( '\'' );
        sb.append( ", taxid=" ).append( taxid );
        sb.append( ", scientificName='" ).append( scientificName ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        TaxonomyTerm newtTerm = ( TaxonomyTerm ) o;

        if ( taxid != newtTerm.taxid ) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return taxid;
    }
}