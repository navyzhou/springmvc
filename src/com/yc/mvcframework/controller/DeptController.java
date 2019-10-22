package com.yc.mvcframework.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.yc.mvcframework.dao.IDeptDao;
import com.yc.mvcframework.entity.Dept;
import com.yc.mvcframework.web.annotation.YCAutowired;
import com.yc.mvcframework.web.annotation.YCController;
import com.yc.mvcframework.web.annotation.YCRequestMapping;
import com.yc.mvcframework.web.annotation.YCRequestParam;
import com.yc.mvcframework.web.annotation.YCResponseBody;

@YCController
@YCRequestMapping("/dept")
public class DeptController {
	@YCAutowired
	private IDeptDao deptDao;
	
	@YCRequestMapping("/findAll")
	@YCResponseBody
	public List<Dept> findAll() {
		return deptDao.finds();
	}
	
	@YCRequestMapping("/find")
	@YCResponseBody
	public Dept find(@YCRequestParam("id") Integer deptno) {
		System.out.println("参数：" + deptno);
		return deptDao.find(deptno);
	}
	
	@YCRequestMapping("/add")
	public String add(Integer deptno, String dname, String loc) {
		System.out.println("参数：" + deptno + "\t" + dname + "\t" + loc);
		return "../show.html";
	}
	
	@YCRequestMapping("/add1")
	public String add1(Dept dept, String name) {
		System.out.println("对象注值：" + dept);
		System.out.println("参数：" + name);
		return "../show.html";
	}
	
	@YCRequestMapping("/add2")
	@YCResponseBody
	public Map<String, Object> add2(Map<String, Object> map) {
		System.out.println("map注值：" + map);
		return map;
	}
	
	@YCRequestMapping("/show1")
	public String show1(String name, HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		System.out.println("session : " + session);
		System.out.println("request : " + session);
		System.out.println("response : " + session);
		
		session.setAttribute("name", name);
		System.out.println("name : " + name);
		return "../show.jsp";
	}
}
