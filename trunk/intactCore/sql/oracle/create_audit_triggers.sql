
spool &spoolFile
set term off
set doc off

begin
   for r_tab in (select lower(table_name) table_name 
                 from   user_tables 
                 where  table_name not like '%AUDIT'
                 and    table_name not in
                        ( 'IA_GODENS_BINARY'
                         ,'IA_GODENS_GODAG'
                         ,'IA_GODENS_GODAGDENORM'
                         ,'IA_GODENS_GOPROT'
                         ,'IA_GODENS_DENSITY'
                        )   
   ) loop
      dbms_output.put_line ('PROMPT Creating audit trigger for '||r_tab.table_name);
      dbms_output.put_line ('create or replace trigger '|| substr ('trgAud_'||r_tab.table_name,1,30)  );
      dbms_output.put_line (chr(9)||'before update or delete');
      dbms_output.put_line (chr(9)||'on '||r_tab.table_name);
      dbms_output.put_line (chr(9)||'for each row');
      dbms_output.put_line (' ');
      dbms_output.put_line ('declare');
      dbms_output.put_line (chr(9)||'l_event char(1);');
      dbms_output.put_line ('begin');
      dbms_output.put_line (' ');
      dbms_output.put_line (chr(9)||'if deleting then    ');
      dbms_output.put_line (chr(9)||chr(9)||'l_event := ''D'';');
      dbms_output.put_line (chr(9)||'elsif updating then ');
      dbms_output.put_line (chr(9)||chr(9)||'l_event := ''U'';');


      for r_tabcols in (select lower(column_name) col
                        from   user_tab_columns
                        where  table_name = upper(r_tab.table_name)
                        and    lower(column_name) in ('updated', 'userstamp')
                        order  by column_id)
      loop
        if  r_tabcols.col = 'updated' then
          dbms_output.put_line (chr(9)||chr(9)||':new.timestamp := sysdate; '); 
        else
          dbms_output.put_line (chr(9)||chr(9)||':new.userstamp := user;  '); 
        end if;
      end loop;

      dbms_output.put_line (chr(9)||'end if ;  '); 
      dbms_output.put_line (chr(9)||''); 
      dbms_output.put_line (chr(9)||''); 

      dbms_output.put_line (chr(9)||'insert into '||r_tab.table_name||'_audit ');
      dbms_output.put_line (chr(9)||chr(9)||'( event ');

--      for r_tabcols in (select lower(column_name) col
--                        from   user_tab_columns
--                        where  table_name = upper(r_tab.table_name)
--                        and    lower(column_name) in ('updated', 'userstamp')
--                        order  by column_id)
--      loop
--        if  r_tabcols.col = 'updated' then
--           dbms_output.put_line (chr(9)||chr(9)||' ,updated   ');
--        else
--           dbms_output.put_line (chr(9)||chr(9)||' ,userstamp ');
--        end if;
--      end loop;

      for r_tabcols in (select lower(column_name) column_name
                        from   user_tab_columns
                        where  table_name = upper(r_tab.table_name)
                        --and    lower(column_name) not in ('updated', 'userstamp')
                        order  by column_id)
      loop
         dbms_output.put_line (chr(9)||chr(9)||', '||r_tabcols.column_name);
      end loop;
      dbms_output.put_line (chr(9)||chr(9)||')');
      dbms_output.put_line (chr(9)||'values');
      dbms_output.put_line (chr(9)||chr(9)||'( l_event ');

--      for r_tabcols in (select lower(column_name) col
--                        from   user_tab_columns
--                        where  table_name = upper(r_tab.table_name)
--                        and    lower(column_name) in ('updated', 'userstamp')
--                        order  by column_id)
--      loop
--        if  r_tabcols.col = 'updated' then
--           dbms_output.put_line (chr(9)||chr(9)||', sysdate ');
--        else
--           dbms_output.put_line (chr(9)||chr(9)||', user    ');
--        end if;
--      end loop;

      for r_tabcols in (select lower(column_name) column_name
                              ,data_type
                        from   user_tab_columns
                        where  table_name = upper(r_tab.table_name)
                        --and    lower(column_name) not in ('updated', 'userstamp')
                        order  by column_id)
      loop
              dbms_output.put_line (chr(9)||chr(9)||', :old.'||r_tabcols.column_name);
      end loop;

      dbms_output.put_line (chr(9)||chr(9)||');');
      dbms_output.put_line ('end;        ');
      dbms_output.put_line ('/           ');
      dbms_output.put_line ('show error');
      dbms_output.put_line (chr(10));
      dbms_output.put_line (chr(10));

   end loop;
end;
/

spool off
set term on
@&spoolFile ;
