/** @author Prayash Rawal */
package com.rescuenet.model;

/**
 * RoleModel represents a role entity in the RescueNet application. It maps to
 * the ROLES table in the database and contains role-related information.
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

	/**
	 * Gets the role ID.
	 *
	 * @return the role ID
	 */
	public int getRoleId() {
		return roleId;
	}

	/**
	 * Sets the role ID.
	 *
	 * @param roleId the role ID to set
	 */
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	/**
	 * Gets the role name.
	 *
	 * @return the role name
	 */
	public String getRoleName() {
		return roleName;
	}

	/**
	 * Sets the role name.
	 *
	 * @param roleName the role name to set
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
}