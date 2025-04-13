package com.rescuenet.model;

/**
 * RoleModel represents a role entity in the RescueNet application.
 * It maps to the ROLES table in the database and contains role-related information.
 */
public class RoleModel {

    private int roleId;
    private String roleName;

    /**
     * Default constructor for RoleModel.
     */
    public RoleModel() {
    }

    /**
     * Constructor with all fields.
     * 
     * @param roleId   the ID of the role
     * @param roleName the name of the role
     */
    public RoleModel(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    // Getters and Setters
    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}