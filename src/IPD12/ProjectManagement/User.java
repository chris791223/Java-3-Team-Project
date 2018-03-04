/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IPD12.ProjectManagement;

/**
 *
 * @author wjing
 */
public class User {

    private long id;
    private boolean isAvailable;
    
    // constructor
    public User(long id, boolean isAvailable) {
        this.id = id;
        this.isAvailable = isAvailable;
    }

     public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }



 // For Jerry
   ////////////////////////////////////////////////////////////////////
   private String name;
   private String email;
   private String ability ;
   private String password ;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public User(long id, String name, String email, String ability,String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.ability = ability;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAbility() {
        return ability;
    }

    public void setAbility(String ability) {
        this.ability = ability;
    }
  
   
    
   ////////////////////////////////////////////////////////////////////

   
    
    
}



