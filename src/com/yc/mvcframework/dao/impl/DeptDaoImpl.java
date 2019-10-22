package com.yc.mvcframework.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.yc.mvcframework.dao.IDeptDao;
import com.yc.mvcframework.entity.Dept;
import com.yc.mvcframework.web.annotation.YCComponent;

@YCComponent
public class DeptDaoImpl implements IDeptDao{
	@Override
	public List<Dept> finds() {
		List<Dept> list = new ArrayList<Dept>();
		Collections.addAll(list, new Dept(101, "行政部", "8601"),
				new Dept(102, "技术部", "8602"),
				new Dept(103, "市场部", "8603"),
				new Dept(104, "财务部", "8604"));
		return list;
	}

	@Override
	public Dept find(Integer deptno) {
		return new Dept(deptno, "技术部", "8602");
	}

	@Override
	public int add(Integer deptno, String dname, String loc) {
		return 1;
	}
}
