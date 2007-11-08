create table ia_annotation (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, description varchar(4000), topic_ac varchar(30), primary key (ac));
create table ia_biosource (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, fullName varchar(250), shortLabel varchar(20) not null, celltype_ac varchar(255), tissue_ac varchar(255), taxId varchar(30), primary key (ac));
create table ia_biosource2annot (biosource_ac varchar(30) not null, annotation_ac varchar(30) not null);
create table ia_biosource_alias (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, name varchar(30), aliastype_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_biosource_xref (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, dbRelease varchar(255), primaryId varchar(255), secondaryId varchar(30), qualifier_ac varchar(30), database_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_component (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, fullName varchar(250), shortLabel varchar(20) not null, expressedin_ac varchar(255), interaction_ac varchar(255), interactor_ac varchar(255), stoichiometry float4 not null, biologicalrole_ac varchar(30), experimentalrole_ac varchar(30), primary key (ac));
create table ia_component2annot (component_ac varchar(30) not null, annotation_ac varchar(30) not null);
create table ia_component2exp_preps (component_ac varchar(30) not null, cvobject_ac varchar(30) not null);
create table ia_component2part_detect (component_ac varchar(30) not null, cvobject_ac varchar(30) not null);
create table ia_component_alias (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, name varchar(30), aliastype_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_component_xref (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, dbRelease varchar(255), primaryId varchar(255), secondaryId varchar(30), qualifier_ac varchar(30), database_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_controlledvocab (objclass varchar(255) not null, ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, fullName varchar(250), shortLabel varchar(20) not null, primary key (ac), unique (objclass, shortLabel));
create table ia_controlledvocab_alias (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, name varchar(30), aliastype_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_controlledvocab_xref (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, dbRelease varchar(255), primaryId varchar(255), secondaryId varchar(30), qualifier_ac varchar(30), database_ac varchar(30), parent_ac varchar(255), primary key (ac), unique (parent_ac, qualifier_ac, database_ac, primaryId));
create table ia_cv2cv (parent_ac varchar(30) not null, child_ac varchar(30) not null);
create table ia_cvobject2annot (cvobject_ac varchar(30) not null, annotation_ac varchar(30) not null);
create table ia_db_info (dbi_key varchar(20) not null, created_date timestamp, created_user varchar(30), updated_date timestamp, updated_user varchar(30), value varchar(20), primary key (dbi_key));
create table ia_exp2annot (experiment_ac varchar(30) not null, annotation_ac varchar(30) not null);
create table ia_experiment (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, fullName varchar(250), shortLabel varchar(20) not null, biosource_ac varchar(255), detectmethod_ac varchar(255), identmethod_ac varchar(30), relatedexperiment_ac varchar(30), publication_ac varchar(30), primary key (ac));
create table ia_experiment_alias (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, name varchar(30), aliastype_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_experiment_xref (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, dbRelease varchar(255), primaryId varchar(255), secondaryId varchar(30), qualifier_ac varchar(30), database_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_feature (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, fullName varchar(250), shortLabel varchar(20) not null, component_ac varchar(30), linkedfeature_ac varchar(30), featuretype_ac varchar(30), identification_ac varchar(30), primary key (ac));
create table ia_feature2annot (feature_ac varchar(30) not null, annotation_ac varchar(30) not null);
create table ia_feature_alias (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, name varchar(30), aliastype_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_feature_xref (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, dbRelease varchar(255), primaryId varchar(255), secondaryId varchar(30), qualifier_ac varchar(30), database_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_imex_import (id int8 not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, activationType varchar(255), count_failed int4, count_not_found int4, count_total int4, import_date timestamp, repository varchar(255), primary key (id));
comment on table ia_imex_import is 'Represents an IMEx import action, which may contain many publications.';
create table ia_imex_import_pub (pmid varchar(50) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, message text, original_filename varchar(255), release_date timestamp, status varchar(255), imexImport_id int8, provider_ac varchar(30), primary key (imexImport_id, pmid));
comment on table ia_imex_import_pub is 'Table used to track the IMEx imported publications';
create table ia_institution (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, fullName varchar(255), postalAddress varchar(255), shortLabel varchar(20) not null, url varchar(255), primary key (ac));
create table ia_institution2annot (institution_ac varchar(30) not null, annotation_ac varchar(30) not null);
create table ia_institution_alias (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, name varchar(30), aliastype_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_institution_xref (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, dbRelease varchar(255), primaryId varchar(255), secondaryId varchar(30), qualifier_ac varchar(30), database_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_int2annot (interactor_ac varchar(30) not null, annotation_ac varchar(30) not null);
create table ia_int2exp (interaction_ac varchar(30) not null, experiment_ac varchar(30) not null);
create table ia_interactions (graphId int4 not null, interaction_ac varchar(255), protein1_ac varchar(255), protein2_ac varchar(255), pubmed_id varchar(255), shortLabel1 varchar(30), shortLabel2 varchar(30), taxid varchar(30), weight float8 not null, experiment_ac varchar(30), detectmethod_ac varchar(30), primary key (interaction_ac, protein1_ac, protein2_ac));
create table ia_interactor (objclass varchar(100) not null, ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, fullName varchar(250), shortLabel varchar(20) not null, crc64 varchar(255), KD float4, interactiontype_ac varchar(255), interactortype_ac varchar(30), biosource_ac varchar(30), primary key (ac));
create table ia_interactor_alias (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, name varchar(30), aliastype_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_interactor_xref (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, dbRelease varchar(255), primaryId varchar(255), secondaryId varchar(30), qualifier_ac varchar(30), database_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_payg (nid varchar(20) not null, species varchar(30) not null, bait int4 not null, econf int4 not null, eseen int4 not null, inDegree int4 not null, outDegree float4 not null, prey int4 not null, really_used_as_bait char(1), primary key (nid, species));
create table ia_payg_current_edge (nidA varchar(20) not null, nidB varchar(20) not null, species varchar(20) not null, conf int4 not null, seen int4 not null, primary key (nidA, nidB, species));
create table ia_payg_temp_node (nid varchar(20) not null, species varchar(30) not null, primary key (nid, species));
create table ia_pub2annot (publication_ac varchar(30) not null, annotation_ac varchar(30) not null);
create table ia_publication (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, fullName varchar(250), shortLabel varchar(20) not null, primary key (ac));
create table ia_publication_alias (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, name varchar(30), aliastype_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_publication_xref (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, dbRelease varchar(255), primaryId varchar(255), secondaryId varchar(30), qualifier_ac varchar(30), database_ac varchar(30), parent_ac varchar(255), primary key (ac));
create table ia_range (ac varchar(30) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, deprecated bool not null, owner_ac varchar(255) not null, fromIntervalEnd int4 not null, fromIntervalStart int4 not null, link char(1), sequence varchar(255), toIntervalEnd int4 not null, toIntervalStart int4 not null, undetermined char(1) not null, tofuzzytype_ac varchar(30), feature_ac varchar(30), fromfuzzytype_ac varchar(30), primary key (ac));
create table ia_search (ac varchar(255) not null, objClass varchar(255) not null, type varchar(255) not null, value varchar(255) not null, primary key (ac, objClass, type, value));
create table ia_sequence_chunk (ac varchar(255) not null, created timestamp not null, created_user varchar(30) not null, updated timestamp not null, userstamp varchar(30) not null, parent_ac varchar(255), sequence_chunk varchar(1000), sequence_index int4, primary key (ac));
alter table ia_annotation add constraint FKA23B94B67C3443CC foreign key (owner_ac) references ia_institution;
alter table ia_annotation add constraint FKA23B94B6EAD2E594 foreign key (topic_ac) references ia_controlledvocab;
alter table ia_biosource add constraint FK51B788BC7C3443CC foreign key (owner_ac) references ia_institution;
alter table ia_biosource add constraint FK51B788BCDFAB074E foreign key (celltype_ac) references ia_controlledvocab;
alter table ia_biosource add constraint FK51B788BCB0B339CE foreign key (tissue_ac) references ia_controlledvocab;
alter table ia_biosource2annot add constraint FK6FE1EE50C7A1E867 foreign key (biosource_ac) references ia_biosource;
alter table ia_biosource2annot add constraint FK6FE1EE5094AB38BB foreign key (annotation_ac) references ia_annotation;
alter table ia_biosource_alias add constraint FK3C6A6307C3443CCbcab050d foreign key (owner_ac) references ia_institution;
alter table ia_biosource_alias add constraint FKBCAB050DB89D9D80 foreign key (parent_ac) references ia_biosource;
alter table ia_biosource_alias add constraint FK3C6A6307B2AE654bcab050d foreign key (aliastype_ac) references ia_controlledvocab;
alter table ia_biosource_xref add constraint FK29B93B7C3443CC81ff8abe foreign key (owner_ac) references ia_institution;
alter table ia_biosource_xref add constraint FK81FF8ABEB89D9D80 foreign key (parent_ac) references ia_biosource;
alter table ia_biosource_xref add constraint FK29B93B8F4052CE81ff8abe foreign key (database_ac) references ia_controlledvocab;
alter table ia_biosource_xref add constraint FK29B93B518575D981ff8abe foreign key (qualifier_ac) references ia_controlledvocab;
alter table ia_component add constraint FKE654A696C48A7AA7 foreign key (interaction_ac) references ia_interactor;
alter table ia_component add constraint FKE654A6967C3443CC foreign key (owner_ac) references ia_institution;
alter table ia_component add constraint FKE654A696DF9A3FAE foreign key (biologicalrole_ac) references ia_controlledvocab;
alter table ia_component add constraint FKE654A6967FAD3B foreign key (interactor_ac) references ia_interactor;
alter table ia_component add constraint FKE654A6968063476 foreign key (expressedin_ac) references ia_biosource;
alter table ia_component add constraint FKE654A6967515B0E foreign key (experimentalrole_ac) references ia_controlledvocab;
alter table ia_component2annot add constraint FK7B83C0AA94AB38BB foreign key (annotation_ac) references ia_annotation;
alter table ia_component2annot add constraint FK7B83C0AAEAAEF607 foreign key (component_ac) references ia_component;
alter table ia_component2exp_preps add constraint FK4B58C268EAAEF607 foreign key (component_ac) references ia_component;
alter table ia_component2exp_preps add constraint FK4B58C26858FA1711 foreign key (cvobject_ac) references ia_controlledvocab;
alter table ia_component2part_detect add constraint FK5E41BA93EAAEF607 foreign key (component_ac) references ia_component;
alter table ia_component2part_detect add constraint FK5E41BA937034AAA foreign key (cvobject_ac) references ia_controlledvocab;
alter table ia_component_alias add constraint FK3C6A6307C3443CCc84cd767 foreign key (owner_ac) references ia_institution;
alter table ia_component_alias add constraint FKC84CD76783D5CF3A foreign key (parent_ac) references ia_component;
alter table ia_component_alias add constraint FK3C6A6307B2AE654c84cd767 foreign key (aliastype_ac) references ia_controlledvocab;
alter table ia_component_xref add constraint FK29B93B7C3443CC380d0524 foreign key (owner_ac) references ia_institution;
alter table ia_component_xref add constraint FK380D052483D5CF3A foreign key (parent_ac) references ia_component;
alter table ia_component_xref add constraint FK29B93B8F4052CE380d0524 foreign key (database_ac) references ia_controlledvocab;
alter table ia_component_xref add constraint FK29B93B518575D9380d0524 foreign key (qualifier_ac) references ia_controlledvocab;
alter table ia_controlledvocab add constraint FKB6DEA2767C3443CC foreign key (owner_ac) references ia_institution;
alter table ia_controlledvocab_alias add constraint FK3C6A6307C3443CC53a26b47 foreign key (owner_ac) references ia_institution;
alter table ia_controlledvocab_alias add constraint FK53A26B47CA830243 foreign key (parent_ac) references ia_controlledvocab;
alter table ia_controlledvocab_alias add constraint FK3C6A6307B2AE65453a26b47 foreign key (aliastype_ac) references ia_controlledvocab;
alter table ia_controlledvocab_xref add constraint FK29B93B7C3443CC23c57544 foreign key (owner_ac) references ia_institution;
alter table ia_controlledvocab_xref add constraint FK23C57544CA830243 foreign key (parent_ac) references ia_controlledvocab;
alter table ia_controlledvocab_xref add constraint FK29B93B8F4052CE23c57544 foreign key (database_ac) references ia_controlledvocab;
alter table ia_controlledvocab_xref add constraint FK29B93B518575D923c57544 foreign key (qualifier_ac) references ia_controlledvocab;
alter table ia_cv2cv add constraint FK4E5DC0EB2121D8F3 foreign key (parent_ac) references ia_controlledvocab;
alter table ia_cv2cv add constraint FK4E5DC0EB8A53F41 foreign key (child_ac) references ia_controlledvocab;
alter table ia_cvobject2annot add constraint FKA9C7608D94AB38BB foreign key (annotation_ac) references ia_annotation;
alter table ia_cvobject2annot add constraint FKA9C7608D249E287B foreign key (cvobject_ac) references ia_controlledvocab;
alter table ia_exp2annot add constraint FKE9E92C8A2F82DDFB foreign key (experiment_ac) references ia_experiment;
alter table ia_exp2annot add constraint FKE9E92C8A94AB38BB foreign key (annotation_ac) references ia_annotation;
alter table ia_experiment add constraint FKF9D58584CFC33227 foreign key (publication_ac) references ia_publication;
alter table ia_experiment add constraint FKF9D58584C7A1E867 foreign key (biosource_ac) references ia_biosource;
alter table ia_experiment add constraint FKF9D585847C3443CC foreign key (owner_ac) references ia_institution;
alter table ia_experiment add constraint FKF9D58584DC019FB0 foreign key (relatedexperiment_ac) references ia_experiment;
alter table ia_experiment add constraint FKF9D585848259E8EB foreign key (identmethod_ac) references ia_controlledvocab;
alter table ia_experiment add constraint FKF9D585848D021C02 foreign key (detectmethod_ac) references ia_controlledvocab;
alter table ia_experiment_alias add constraint FK3C6A6307C3443CC113a4bd5 foreign key (owner_ac) references ia_institution;
alter table ia_experiment_alias add constraint FK113A4BD59670D62E foreign key (parent_ac) references ia_experiment;
alter table ia_experiment_alias add constraint FK3C6A6307B2AE654113a4bd5 foreign key (aliastype_ac) references ia_controlledvocab;
alter table ia_experiment_xref add constraint FK29B93B7C3443CCbe884af6 foreign key (owner_ac) references ia_institution;
alter table ia_experiment_xref add constraint FKBE884AF69670D62E foreign key (parent_ac) references ia_experiment;
alter table ia_experiment_xref add constraint FK29B93B8F4052CEbe884af6 foreign key (database_ac) references ia_controlledvocab;
alter table ia_experiment_xref add constraint FK29B93B518575D9be884af6 foreign key (qualifier_ac) references ia_controlledvocab;
alter table ia_feature add constraint FKB23F96CF7C3443CC foreign key (owner_ac) references ia_institution;
alter table ia_feature add constraint FKB23F96CF98CC83CA foreign key (identification_ac) references ia_controlledvocab;
alter table ia_feature add constraint FKB23F96CFEAAEF607 foreign key (component_ac) references ia_component;
alter table ia_feature add constraint FKB23F96CF92341BD4 foreign key (featuretype_ac) references ia_controlledvocab;
alter table ia_feature add constraint FKB23F96CF925503C0 foreign key (linkedfeature_ac) references ia_feature;
alter table ia_feature2annot add constraint FKCEBDE2239D5068E7 foreign key (feature_ac) references ia_feature;
alter table ia_feature2annot add constraint FKCEBDE22394AB38BB foreign key (annotation_ac) references ia_annotation;
alter table ia_feature_alias add constraint FK3C6A6307C3443CC1b86f8e0 foreign key (owner_ac) references ia_institution;
alter table ia_feature_alias add constraint FK1B86F8E021B28EB3 foreign key (parent_ac) references ia_feature;
alter table ia_feature_alias add constraint FK3C6A6307B2AE6541b86f8e0 foreign key (aliastype_ac) references ia_controlledvocab;
alter table ia_feature_xref add constraint FK29B93B7C3443CC3abc508b foreign key (owner_ac) references ia_institution;
alter table ia_feature_xref add constraint FK3ABC508B21B28EB3 foreign key (parent_ac) references ia_feature;
alter table ia_feature_xref add constraint FK29B93B8F4052CE3abc508b foreign key (database_ac) references ia_controlledvocab;
alter table ia_feature_xref add constraint FK29B93B518575D93abc508b foreign key (qualifier_ac) references ia_controlledvocab;
alter table ia_imex_import_pub add constraint fk_ImexImport_imexImportPub foreign key (imexImport_id) references ia_imex_import;
alter table ia_imex_import_pub add constraint fk_Institution_provider foreign key (provider_ac) references ia_institution;
alter table ia_institution2annot add constraint FK98043EE5B29FF6A7 foreign key (institution_ac) references ia_institution;
alter table ia_institution2annot add constraint FK98043EE594AB38BB foreign key (annotation_ac) references ia_annotation;
alter table ia_institution_alias add constraint FK3C6A6307C3443CCe4cd55a2 foreign key (owner_ac) references ia_institution;
alter table ia_institution_alias add constraint FKE4CD55A294795675 foreign key (parent_ac) references ia_institution;
alter table ia_institution_alias add constraint FKE4CD55A221B28EB3 foreign key (parent_ac) references ia_feature;
alter table ia_institution_alias add constraint FK3C6A6307B2AE654e4cd55a2 foreign key (aliastype_ac) references ia_controlledvocab;
alter table ia_institution_xref add constraint FK29B93B7C3443CC30b65389 foreign key (owner_ac) references ia_institution;
alter table ia_institution_xref add constraint FK30B6538994795675 foreign key (parent_ac) references ia_institution;
alter table ia_institution_xref add constraint FK29B93B8F4052CE30b65389 foreign key (database_ac) references ia_controlledvocab;
alter table ia_institution_xref add constraint FK29B93B518575D930b65389 foreign key (qualifier_ac) references ia_controlledvocab;
alter table ia_int2annot add constraint FKFFC814DC94AB38BB foreign key (annotation_ac) references ia_annotation;
alter table ia_int2annot add constraint FKFFC814DC7FAD3B foreign key (interactor_ac) references ia_interactor;
alter table ia_int2exp add constraint FK613B68F32F82DDFB foreign key (experiment_ac) references ia_experiment;
alter table ia_int2exp add constraint FK613B68F3C48A7AA7 foreign key (interaction_ac) references ia_interactor;
alter table ia_interactions add constraint FK421658682F82DDFB foreign key (experiment_ac) references ia_experiment;
alter table ia_interactions add constraint FK42165868C48A7AA7 foreign key (interaction_ac) references ia_interactor;
alter table ia_interactions add constraint FK421658684259E705 foreign key (protein2_ac) references ia_interactor;
alter table ia_interactions add constraint FK42165868425972A6 foreign key (protein1_ac) references ia_interactor;
alter table ia_interactions add constraint FK4216586889558D5F foreign key (detectmethod_ac) references ia_controlledvocab;
alter table ia_interactor add constraint FK9F5E9820C7A1E867 foreign key (biosource_ac) references ia_biosource;
alter table ia_interactor add constraint FK9F5E98207C3443CC foreign key (owner_ac) references ia_institution;
alter table ia_interactor add constraint FK9F5E9820E8A4322E foreign key (interactortype_ac) references ia_controlledvocab;
alter table ia_interactor add constraint FK9F5E9820C6904D4 foreign key (interactiontype_ac) references ia_controlledvocab;
alter table ia_interactor_alias add constraint FK3C6A6307C3443CC1879f971 foreign key (owner_ac) references ia_institution;
alter table ia_interactor_alias add constraint FK1879F971DD210D8A foreign key (parent_ac) references ia_interactor;
alter table ia_interactor_alias add constraint FK3C6A6307B2AE6541879f971 foreign key (aliastype_ac) references ia_controlledvocab;
alter table ia_interactor_xref add constraint FK29B93B7C3443CCe80e79da foreign key (owner_ac) references ia_institution;
alter table ia_interactor_xref add constraint FKE80E79DADD210D8A foreign key (parent_ac) references ia_interactor;
alter table ia_interactor_xref add constraint FK29B93B8F4052CEe80e79da foreign key (database_ac) references ia_controlledvocab;
alter table ia_interactor_xref add constraint FK29B93B518575D9e80e79da foreign key (qualifier_ac) references ia_controlledvocab;
alter table ia_pub2annot add constraint FK2C9F2D6ACFC33227 foreign key (publication_ac) references ia_publication;
alter table ia_pub2annot add constraint FK2C9F2D6A94AB38BB foreign key (annotation_ac) references ia_annotation;
alter table ia_publication add constraint FK9E4529857C3443CC foreign key (owner_ac) references ia_institution;
alter table ia_publication_alias add constraint FK3C6A6307C3443CC19452b16 foreign key (owner_ac) references ia_institution;
alter table ia_publication_alias add constraint FK19452B161F0B52E9 foreign key (parent_ac) references ia_publication;
alter table ia_publication_alias add constraint FK3C6A6307B2AE65419452b16 foreign key (aliastype_ac) references ia_controlledvocab;
alter table ia_publication_xref add constraint FK29B93B7C3443CC63f3ff95 foreign key (owner_ac) references ia_institution;
alter table ia_publication_xref add constraint FK63F3FF951F0B52E9 foreign key (parent_ac) references ia_publication;
alter table ia_publication_xref add constraint FK29B93B8F4052CE63f3ff95 foreign key (database_ac) references ia_controlledvocab;
alter table ia_publication_xref add constraint FK29B93B518575D963f3ff95 foreign key (qualifier_ac) references ia_controlledvocab;
alter table ia_range add constraint FK4F2877567C3443CC foreign key (owner_ac) references ia_institution;
alter table ia_range add constraint FK4F2877569D5068E7 foreign key (feature_ac) references ia_feature;
alter table ia_range add constraint FK4F2877569BD41CBE foreign key (fromfuzzytype_ac) references ia_controlledvocab;
alter table ia_range add constraint FK4F287756FE5B620F foreign key (tofuzzytype_ac) references ia_controlledvocab;
create index i_ia_search on ia_search (value, objClass);
alter table ia_sequence_chunk add constraint FKB1ABF6764E4CFA8B foreign key (parent_ac) references ia_interactor;
create sequence imex_sequence;
create sequence intact_ac;

