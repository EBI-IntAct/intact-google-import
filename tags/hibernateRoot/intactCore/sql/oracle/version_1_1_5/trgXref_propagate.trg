CREATE OR REPLACE TRIGGER trgXref_propagate
   AFTER INSERT OR UPDATE OR DELETE
   ON IA_XREF
   FOR EACH ROW
BEGIN
    /*ac will be unique over all tables, so only updates for relevant table */
   IF INSERTING THEN
      UPDATE IA_EXPERIMENT
      SET UPDATED = SYSDATE
      WHERE AC= :NEW.parent_ac;

      UPDATE IA_INTERACTOR
      SET UPDATED = SYSDATE
      WHERE AC= :NEW.parent_ac;

      UPDATE IA_FEATURE
      SET UPDATED = SYSDATE
      WHERE AC= :NEW.parent_ac;

   ELSIF UPDATING THEN
      UPDATE IA_EXPERIMENT
      SET UPDATED = SYSDATE
      WHERE AC= :OLD.parent_ac
      OR      AC =:NEW.parent_ac;

      UPDATE IA_INTERACTOR
      SET UPDATED = SYSDATE
      WHERE AC= :OLD.parent_ac
      OR      AC =:NEW.parent_ac;

      UPDATE IA_FEATURE
      SET UPDATED = SYSDATE
      WHERE AC= :OLD.parent_ac
      OR      AC =:NEW.parent_ac;

   ELSIF DELETING THEN
      UPDATE IA_EXPERIMENT
      SET UPDATED = SYSDATE
      WHERE AC= :OLD.parent_ac;

      UPDATE IA_INTERACTOR
      SET UPDATED = SYSDATE
      WHERE AC= :OLD.parent_ac;

      UPDATE IA_FEATURE
      SET UPDATED = SYSDATE
      WHERE AC= :OLD.parent_ac;
   END IF ;
END;
/
