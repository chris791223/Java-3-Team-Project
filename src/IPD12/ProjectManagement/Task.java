/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IPD12.ProjectManagement;

import java.util.Date;

/**
 *
 * @author wjing
 */
public class Task {
    private long id;
    private String name;
    private String description;
    private Date startDatePlanned;
    private Date endDatePlanned;
    private Date startDateActual;
    private Date endDateActual;
    private long personInCharge;
    private boolean isCompleted;

    // constructor
    public Task(long id, String name, String description, Date startDatePlanned, Date endDatePlanned, Date startDateActual, Date endDateActual, long personInCharge, boolean isCompleted) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDatePlanned = startDatePlanned;
        this.endDatePlanned = endDatePlanned;
        this.startDateActual = startDateActual;
        this.endDateActual = endDateActual;
        this.personInCharge = personInCharge;
        this.isCompleted = isCompleted;
    }

    // setters and getters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDatePlanned() {
        return startDatePlanned;
    }

    public void setStartDatePlanned(Date startDatePlanned) {
        this.startDatePlanned = startDatePlanned;
    }

    public Date getEndDatePlanned() {
        return endDatePlanned;
    }

    public void setEndDatePlanned(Date endDatePlanned) {
        this.endDatePlanned = endDatePlanned;
    }

    public Date getStartDateActual() {
        return startDateActual;
    }

    public void setStartDateActual(Date startDateActual) {
        this.startDateActual = startDateActual;
    }

    public Date getEndDateActual() {
        return endDateActual;
    }

    public void setEndDateActual(Date endDateActual) {
        this.endDateActual = endDateActual;
    }

    public long getPersonInCharge() {
        return personInCharge;
    }

    public void setPersonInCharge(long personInCharge) {
        this.personInCharge = personInCharge;
    }

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
    
   // For Jerry
   ////////////////////////////////////////////////////////////////////
    
    
   ////////////////////////////////////////////////////////////////////
    
}
