package com.yc.mvcframework.dao;

import java.util.List;

import com.yc.mvcframework.entity.Dept;

public interface IDeptDao {
	/**
	 * 查询所有部门信息
	 * @return
	 */
	public List<Dept> finds();

	/**
	 * 根据部门信息查询
	 * @param deptno
	 * @return
	 */
	public Dept find(Integer deptno);

	/**
	 * 添加部门信息
	 * @param deptno
	 * @param dname
	 * @param loc
	 * @return
	 */
	public int add(Integer deptno, String dname, String loc);
}
