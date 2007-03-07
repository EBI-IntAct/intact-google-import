/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.protein.mock;

import uk.ac.ebi.intact.uniprot.model.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Mock Uniprot Protein
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.1.2
 */
public class MockUniprotProteinBuilder {

    /////////////////////////
    // Instance attributes

    /**
     * Uniprot protein ID.
     */
    private String id;

    /**
     * Primary Accession Number.
     */
    private String primaryAc;

    /**
     * Ordered list of secondary Accession Numbers.
     */
    private List<String> secondaryAcs;

    /**
     * Organism of the protein.
     */
    private Organism organism;

    /**
     * Description of the protein.
     */
    private String description;

    /**
     * Collection of gene name.
     */
    private Collection<String> genes;

    /**
     * Collection of ORF name.
     */
    private Collection<String> orfs;

    /**
     * Collection of synonyms.
     */
    private Collection<String> synomyms;

    /**
     * Collection of locus name.
     */
    private Collection<String> locuses;

    /**
     * Collection of releated diseases.
     */
    private Collection<String> diseases;

    /**
     * Collection of keywords.
     */
    private Collection<String> keywords;

    /**
     * Known function of that protein.
     */
    private Collection<String> functions;

    /**
     * Collection of cross references.
     */
    private Collection<UniprotXref> crossReferences;

    /**
     * Collection of Splice variant.
     */
    private Collection<UniprotSpliceVariant> spliceVariants;

    /**
     * Collection of feature chain
     */
    private Collection<UniprotFeatureChain> featureChains;

    // desease, functions

    /**
     * Hashing og the sequence generated using CRC64 algorithm.
     */
    private String crc64;

    /**
     * Amino Acis sequence of the protein.
     */
    private String sequence;

    /**
     * Length of the sequence.
     */
    private int sequenceLength;

    /**
     * Release version of the protein.
     */
    private String releaseVersion;

    /**
     * Date at which the annotation was last updated.
     */
    private Date lastAnnotationUpdate;

    /**
     * Date at which the sequence was last updated.
     */
    private Date lastSequenceUpdate;

    /**
     * Where the entry is coming from .
     */
    private UniprotProteinType source;

    ////////////////////////////
    // Constructor

    public MockUniprotProteinBuilder() {
    }

    ////////////////////////////
    // Setters


    public MockUniprotProteinBuilder setCrc64( String crc64 ) {
        this.crc64 = crc64;
        return this;
    }

    public MockUniprotProteinBuilder setCrossReferences( Collection<UniprotXref> crossReferences ) {
        this.crossReferences = crossReferences;
        return this;
    }

    public MockUniprotProteinBuilder setDescription( String description ) {
        this.description = description;
        return this;
    }

    public MockUniprotProteinBuilder setDiseases( Collection<String> diseases ) {
        this.diseases = diseases;
        return this;
    }

    public MockUniprotProteinBuilder setFeatureChains( Collection<UniprotFeatureChain> featureChains ) {
        this.featureChains = featureChains;
        return this;
    }

    public MockUniprotProteinBuilder setFunctions( Collection<String> functions ) {
        this.functions = functions;
        return this;
    }

    public MockUniprotProteinBuilder setGenes( Collection<String> genes ) {
        this.genes = genes;
        return this;
    }

    public MockUniprotProteinBuilder setId( String id ) {
        this.id = id;
        return this;
    }

    public MockUniprotProteinBuilder setKeywords( Collection<String> keywords ) {
        this.keywords = keywords;
        return this;
    }

    public MockUniprotProteinBuilder setLastAnnotationUpdate( Date lastAnnotationUpdate ) {
        this.lastAnnotationUpdate = lastAnnotationUpdate;
        return this;
    }

    public MockUniprotProteinBuilder setLastSequenceUpdate( Date lastSequenceUpdate ) {
        this.lastSequenceUpdate = lastSequenceUpdate;
        return this;
    }

    public MockUniprotProteinBuilder setLocuses( Collection<String> locuses ) {
        this.locuses = locuses;
        return this;
    }

    public MockUniprotProteinBuilder setOrfs( Collection<String> orfs ) {
        this.orfs = orfs;
        return this;
    }

    public MockUniprotProteinBuilder setOrganism( Organism organism ) {
        this.organism = organism;
        return this;
    }

    public MockUniprotProteinBuilder setPrimaryAc( String primaryAc ) {
        this.primaryAc = primaryAc;
        return this;
    }

    public MockUniprotProteinBuilder setReleaseVersion( String releaseVersion ) {
        this.releaseVersion = releaseVersion;
        return this;
    }

    public MockUniprotProteinBuilder setSecondaryAcs( List<String> secondaryAcs ) {
        this.secondaryAcs = secondaryAcs;
        return this;
    }

    public MockUniprotProteinBuilder setSequence( String sequence ) {
        this.sequence = sequence;
        return this;
    }

    public MockUniprotProteinBuilder setSequenceLength( int sequenceLength ) {
        this.sequenceLength = sequenceLength;
        return this;
    }

    public MockUniprotProteinBuilder setSource( UniprotProteinType source ) {
        this.source = source;
        return this;
    }

    public MockUniprotProteinBuilder setSpliceVariants( Collection<UniprotSpliceVariant> spliceVariants ) {
        this.spliceVariants = spliceVariants;
        return this;
    }

    public MockUniprotProteinBuilder setSynomyms( Collection<String> synomyms ) {
        this.synomyms = synomyms;
        return this;
    }

    public UniprotProtein build() {
        UniprotProtein up = new UniprotProtein( id, primaryAc, organism, description );

        if ( secondaryAcs != null ) up.getSecondaryAcs().addAll( secondaryAcs );
        up.setCrc64( crc64 );
        up.setLastAnnotationUpdate( lastAnnotationUpdate );
        up.setLastSequenceUpdate( lastSequenceUpdate );
        up.setReleaseVersion( releaseVersion );
        up.setSequence( sequence );
        up.setSequenceLength( sequenceLength );
        up.setSource( source );
        if ( crossReferences != null ) up.getCrossReferences().addAll( crossReferences );

        if ( spliceVariants != null ) up.getSpliceVariants().addAll( spliceVariants );
        if ( featureChains != null ) up.getFeatureChains().addAll( featureChains );

        if ( keywords != null ) up.getKeywords().addAll( keywords );
        if ( functions != null ) up.getFunctions().addAll( functions );
        if ( locuses != null ) up.getDiseases().addAll( diseases );

        if ( genes != null ) up.getGenes().addAll( genes );
        if ( orfs != null ) up.getOrfs().addAll( orfs );
        if ( synomyms != null ) up.getSynomyms().addAll( synomyms );
        if ( locuses != null ) up.getLocuses().addAll( locuses );

        return up;
    }
}