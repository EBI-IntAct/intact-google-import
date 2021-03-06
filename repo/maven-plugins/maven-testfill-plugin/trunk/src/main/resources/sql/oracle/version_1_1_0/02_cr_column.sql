SET serveroutput ON

spool 01_cr_column.LOG

SELECT TO_CHAR(SYSDATE,'dd-mon-yyyy hh24:mi:ss')  start_date FROM dual;


ALTER TABLE IA_ALIAS ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_ALIAS_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_ANNOTATION ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_ANNOTATION_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_BIOSOURCE ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_BIOSOURCE2ANNOT ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_BIOSOURCE2ANNOT_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_BIOSOURCE_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_COMPONENT ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_COMPONENT_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_CONTROLLEDVOCAB ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_CONTROLLEDVOCAB_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_CV2CV ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_CV2CV_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_CVOBJECT2ANNOT ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_CVOBJECT2ANNOT_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_EXP2ANNOT ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_EXP2ANNOT_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_EXPERIMENT ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_EXPERIMENT_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_FEATURE ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_FEATURE2ANNOT ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_FEATURE2ANNOT_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_FEATURE_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_INSTITUTION ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_INSTITUTION_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_INT2ANNOT ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_INT2ANNOT_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_INT2EXP ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_INT2EXP_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_INTACTNODE ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_INTACTNODE_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_INTERACTOR ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_INTERACTOR_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_PUBMED ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_PUBMED_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_RANGE ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_RANGE_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_SEQUENCE_CHUNK ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_SEQUENCE_CHUNK_AUDIT ADD created_user VARCHAR2(30 BYTE);
ALTER TABLE IA_XREF ADD created_user VARCHAR2(30 BYTE) DEFAULT USER NOT NULL;
ALTER TABLE IA_XREF_AUDIT ADD created_user VARCHAR2(30 BYTE);

ALTER TABLE IA_SEQUENCE_CHUNK ADD updated DATE DEFAULT SYSDATE NOT NULL;
ALTER TABLE IA_SEQUENCE_CHUNK ADD created DATE DEFAULT SYSDATE NOT NULL;

ALTER TABLE IA_SEQUENCE_CHUNK_AUDIT ADD updated DATE;
ALTER TABLE IA_SEQUENCE_CHUNK_AUDIT ADD created DATE;


CREATE TABLE IA_DB_INFO (
	 dbi_key		    VARCHAR2(20)	NOT NULL PRIMARY KEY
	,value			    VARCHAR2(20)	NOT NULL
	,created_date		DATE		    DEFAULT  SYSDATE 	NOT NULL
	,created_user		VARCHAR2(30)	DEFAULT  USER    	NOT NULL
	,updated_date		DATE		    DEFAULT  SYSDATE 	NOT NULL
	,updated_user		VARCHAR2(30)	DEFAULT  USER    	NOT NULL
);


CREATE TABLE IA_DB_INFO_AUDIT (
	event			    CHAR(1)
	,dbi_key		    VARCHAR2(20)
	,value			    VARCHAR2(20)
	,created_date		DATE		 NOT NULL
	,created_user		VARCHAR2(30) NOT NULL
	,updated_date		DATE		 NOT NULL
	,updated_user		VARCHAR2(30) NOT NULL
);

INSERT INTO IA_DB_INFO (
	 dbi_key
	,value
)
VALUES
(	 'schema_version'
	,'1.1.0'
);

COMMIT;

SELECT TO_CHAR(SYSDATE,'dd-mon-yyyy hh24:mi:ss')  end_date FROM dual;

spool OFF
