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
public class Team {
    private long id;
    private String name;
    private String ability;

    // constructor
    public Team(long id, String name, String ability) {
        this.id = id;
        this.name = name;
        this.ability = ability;
    }

    // getters & setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getAbility() {
        return ability;
    }

    public void setAbility(String ability) {
        this.ability = ability;
    }

    @Override
    public String toString() {
        return id + "  " + name + "  " + ability;
    }
    
    public String getIdName() {
        return id + "  " + name;
    }

    //For Jerry
    //////////////////////////////////////////////////////////////////////////
    
    
    
    //////////////////////////////////////////////////////////////////////////
    
}
