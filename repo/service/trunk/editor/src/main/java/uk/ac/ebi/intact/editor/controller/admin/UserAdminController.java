package uk.ac.ebi.intact.editor.controller.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.DualListModel;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.users.model.Role;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.core.users.persistence.dao.UserDao;
import uk.ac.ebi.intact.core.users.persistence.dao.UsersDaoFactory;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Controller used when administrating users.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
@Controller
@Scope( "conversation.access" )
public class UserAdminController extends JpaAwareController {

    private static final Log log = LogFactory.getLog( UserAdminController.class );

    @Autowired
    private UsersDaoFactory daoFactory;

    // User creation

    private String loginParam;
//    private String userEmail;
//    private String userFirstName;
//    private String userLastName;
//
//    private Collection<String> userRoles;

    // roles

    private DualListModel<String> roles;

    // User update

    private User user;

    // User list

    private LazyDataModel<User> allUsers;


    /////////////////
    // Users


    public User getUser() {
        return user;
    }

    public void setUser( User user ) {
        this.user = user;
    }

    public String getLoginParam() {
        return loginParam;
    }

    public void setLoginParam( String loginParam ) {
        this.loginParam = loginParam;
    }

    //    public String getUserLogin() {
//        return userLogin;
//    }
//
//    public void setUserLogin( String userLogin ) {
//        this.userLogin = userLogin;
//    }
//
//    public String getUserEmail() {
//        return userEmail;
//    }
//
//    public void setUserEmail( String userEmail ) {
//        this.userEmail = userEmail;
//    }
//
//    public String getUserFirstName() {
//        return userFirstName;
//    }
//
//    public void setUserFirstName( String userFirstName ) {
//        this.userFirstName = userFirstName;
//    }
//
//    public String getUserLastName() {
//        return userLastName;
//    }
//
//    public void setUserLastName( String userLastName ) {
//        this.userLastName = userLastName;
//    }

    ///////////////
    // Actions


    @Transactional( "users" )
    public String saveUser() {
        final UserDao userDao = daoFactory.getUserDao();

        boolean created = false;
        if( ! userDao.isManaged( user ) && ! userDao.isDetached( user ) ) {
            userDao.persist( user );
            created = true;
        }

        // handle roles
        final List<String> includedRoles = roles.getTarget();
        for ( String roleName : includedRoles ) {
            if( ! user.hasRole( roleName ) ) {
                final Role r = getUsersDaoFactory().getRoleDao().getRoleByName( roleName );
                user.addRole( r );
                log.info( "Added role " + roleName + "to user " + user.getLogin() );
            }
        }

        final List<String> excludedRoles = roles.getSource();
        for ( String roleName : excludedRoles ) {
            if( user.hasRole( roleName ) ) {
                final Role r = getUsersDaoFactory().getRoleDao().getRoleByName( roleName );
                user.removeRole( r );
                log.info( "Removed role " + roleName + "to user " + user.getLogin() );
            }
        }

        userDao.saveOrUpdate( user );

        addInfoMessage( "User " + user.getLogin() + " was "+ (created ? "created" : "updated" ) +" successfully", "" );

        return "admin.users.list";
    }

    public void loadRoles() {

        List<String> source = new ArrayList<String>();
        List<String> target = new ArrayList<String>();
        
        Collection<Role> allRoles = getUsersDaoFactory().getRoleDao().getAll();
        log.info( "Found " + allRoles.size() + " role(s) in the database." );
        if( user == null ) {
            for ( Role role : allRoles ) {
                source.add( role.getName() );
            }
        } else {
            for ( Role role : allRoles ) {
                if( user.getRoles().contains( role )) {
                    target.add( role.getName() );
                } else {
                    source.add( role.getName() );
                }
            }
        }

        roles = new DualListModel<String>( source, target );
    }

    public List<Role> createRoleList( User user) {
        if( user != null ) {
            return new ArrayList<Role>( user.getRoles() );
        }
        return null;
    }

    public void setRoles( DualListModel<String> roles ) {
        this.roles = roles;
    }

    public DualListModel<String> getRoles() {
        return roles;
    }

    public void loadUserToUpdate() {

        if( loginParam != null ) {
            // load user and prepare for update
            System.out.println( "Loading user by login '" + loginParam + "'..." );
            user = getUsersDaoFactory().getUserDao().getByLogin( loginParam );
            if (user == null ) {
                addWarningMessage( "Could not find user by login: " + loginParam, "Please try again." );
            }
        } else {
            // prepare for the creation of the new user
            user = new User();
        }
    }

    public void loadData( ComponentSystemEvent event ) {
        allUsers = LazyDataModelFactory.createLazyDataModel( getUsersEntityManager(),
                                                             "select u from User u order by u.login asc",
                                                             "select count(u) from User u" );
    }

    public LazyDataModel<User> getAllUsers() {
        return allUsers;
    }
}
