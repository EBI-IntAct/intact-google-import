CREATE OR REPLACE TRIGGER trgFeature2annot_propagate
   AFTER INSERT OR UPDATE OR DELETE
   ON IA_FEATURE2ANNOT
   FOR EACH ROW

BEGIN
   IF INSERTING THEN
      UPDATE IA_FEATURE
      SET UPDATED = SYSDATE
      WHERE AC= :NEW.feature_ac;
   ELSIF UPDATING THEN
      UPDATE IA_FEATURE
      SET UPDATED = SYSDATE
      WHERE AC= :OLD.feature_ac
      OR      AC =:NEW.feature_ac;
   ELSIF DELETING THEN
      BEGIN
         UPDATE IA_FEATURE
         SET UPDATED = SYSDATE
         WHERE AC= :OLD.feature_ac;
      EXCEPTION
         WHEN OTHERS THEN
           IF SQLCODE = -04091 /* because of cascade delete in IA_FEATURE.  */
           THEN NULL;
           ELSE
               RAISE;
           END IF;
      END;
   END IF;
END;
/
