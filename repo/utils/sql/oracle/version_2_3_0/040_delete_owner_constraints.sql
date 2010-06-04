PROMPT Removing owner constraint for the entities that do not have an institution anymore
alter table IA_BIOSOURCE drop constraint FK_BIOSOURCE$OWNER;                               
alter table IA_BIOSOURCE_ALIAS drop constraint FK_BIOSOURCE_ALIAS$OWNER;                   
alter table IA_BIOSOURCE_XREF drop constraint FK_BIOSOURCEXREF$OWNER;                      
alter table IA_COMPONENT drop constraint FK_COMPONENT$OWNER;                               
alter table IA_COMPONENT_ALIAS drop constraint FK_COMPONENT_ALIAS$OWNER;                   
alter table IA_COMPONENT_PARAMETER drop constraint FK_COMP_PARAM$OWNER;                    
alter table IA_COMPONENT_XREF drop constraint FK_COMPONENTXREF$OWNER;                      
alter table IA_CONFIDENCE drop constraint FK_CONFIDENCE$OWNER;                             
alter table IA_CONTROLLEDVOCAB drop constraint FK_CONTROLLEDVOCAB$OWNER;                   
alter table IA_CONTROLLEDVOCAB_ALIAS drop constraint FK_CV_ALIAS$OWNER;                    
alter table IA_CONTROLLEDVOCAB_XREF drop constraint FK_CVOBJECTXREF$OWNER;                        
alter table IA_EXPERIMENT_ALIAS drop constraint FK_EXPERIMENT_ALIAS$OWNER;                 
alter table IA_EXPERIMENT_XREF drop constraint FK_EXPERIMENTXREF$OWNER;                    
alter table IA_FEATURE drop constraint FK_FEATURE$OWNER;                                   
alter table IA_FEATURE_ALIAS drop constraint FK_FEATURE_ALIAS$OWNER;                       
alter table IA_FEATURE_XREF drop constraint FK_FEATUREXREF$OWNER;                          
alter table IA_INSTITUTION drop constraint FK_INSTITUTION$OWNER;                           
alter table IA_INSTITUTION_ALIAS drop constraint FK_INSTITUTION_ALIAS$OWNER;               
alter table IA_INSTITUTION_XREF drop constraint FK_INSTITUTIONXREF$OWNER;                  
alter table IA_INTACTNODE drop constraint FK_INTACTNODE$OWNER;                             
alter table IA_INTERACTION_PARAMETER drop constraint FK_INT_PARAM$OWNER;                       
alter table IA_INTERACTOR_ALIAS drop constraint FK_INTERACTOR_ALIAS$OWNER;                 
alter table IA_INTERACTOR_XREF drop constraint FK_INTERACTORXREF$OWNER;                      
alter table IA_PUBLICATION_ALIAS drop constraint FK_PUBLICATION_ALIAS$OWNER;               
alter table IA_PUBLICATION_XREF drop constraint FK_PUBLICATIONXREF$OWNER;                  
alter table IA_RANGE drop constraint FK_RANGE$OWNER;                                       
alter table IA_ALIAS drop constraint FK_ALIAS$OWNER;                                       
alter table IA_ANNOTATION drop constraint FK_ANNOTATION$OWNER;              
commit;
