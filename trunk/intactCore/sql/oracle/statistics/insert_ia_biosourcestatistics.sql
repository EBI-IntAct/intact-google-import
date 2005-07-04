/*
  Copyright (c) 2002 The European Bioinformatics Institute, and others.
  All rights reserved. Please see the file LICENSE
  in the root directory of this distribution.
*/
/*************************************************************

  Package:    IntAct reports

  Purpose:   Anonymous PL/SQL block allowing to update regularly the BioSource Statistics table
             for the statisticView Application


  $Date$
  $Author$
  $Id$
  
*************************************************************/

SET   SERVEROUT ON
SET   FEEDBACK OFF
SET   VERIFY OFF
SET   LINES 150
SET   PAGES 20000
SET   DOC OFF


DECLARE

  cursor c_bin (b_taxid ia_biosource.taxid%TYPE)
  IS
          SELECT count(distinct(component1.interaction_ac)) AS binary_interactions
          FROM ia_interactor i1, ia_component component1
          WHERE 2 = (SELECT COUNT(*)
                     FROM ia_component component2
                     WHERE component1.interaction_ac = component2.interaction_ac) AND
                i1.biosource_ac in (SELECT ac
                                    FROM ia_biosource
                                    WHERE taxid = b_taxid) AND
                component1.interactor_ac = i1.ac ;

   cursor c_prot (b_taxid ia_biosource.taxid%TYPE)
   IS
          SELECT count(distinct(i2.ac)) AS proteins
          FROM ia_interactor i2
          WHERE i2.biosource_ac in (SELECT ac
                                    FROM ia_biosource
                                    WHERE taxid = b_taxid) AND
                i2.objclass ='uk.ac.ebi.intact.model.ProteinImpl' ;

   cnt_bin INTEGER;
   cnt_prot INTEGER;

BEGIN

   dbms_output.enable ( 1000000 );

   -- select the biosources but only those that have no CellType or Tissue
   -- the work around here is to select from a pool of BioSource having the same taxid
   -- the one with the shortest shortlabel.
   FOR r in (SELECT DISTINCT taxid, shortlabel
             FROM ia_biosource b1
             WHERE length(shortlabel) = (SELECT min(length(shortlabel))
                                         FROM ia_biosource b2
                                         WHERE b1.taxid=b2.taxid)
             order by shortlabel)
   LOOP
       dbms_output.put_line ( 'Processing taxid: ' || r.taxid );

       OPEN c_bin (r.taxid);
       FETCH c_bin INTO cnt_bin;
       CLOSE c_bin;

       OPEN c_prot (r.taxid);
       FETCH c_prot INTO cnt_prot;
       CLOSE c_prot;

       dbms_output.put_line ( 'Inserting statement...' );
       INSERT INTO ia_biosourcestatistics(ac, taxid, shortlabel, protein_number, binary_interactions)
       VALUES (Intact_statistics_seq.nextval, r.taxid, r.shortlabel, cnt_bin, cnt_prot);

   END LOOP;

END;
/

exit
