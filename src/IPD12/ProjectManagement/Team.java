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
    private long projectId;  // project id
    private long id;         // member id
    private String name;
    private String ability;
    private boolean isLeft;

    // constructor
    public Team(long id, String name, String ability) {
        this.id = id;
        this.name = name;
        this.ability = ability;
    }

    public Team(long projectId, long id, boolean isLeft) {
        this.projectId = projectId;
        this.id = id;
        this.isLeft = isLeft;
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

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public boolean getIsLeft() {
        return isLeft;
    }

    public void setIsLeft(boolean isLeft) {
        this.isLeft = isLeft;
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
