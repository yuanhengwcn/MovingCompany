package com.hlbrc.movingcompany.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hlbrc.movingcompany.entity.AuthorityManager;
import com.hlbrc.movingcompany.entity.AuthorityManagerExample;
import com.hlbrc.movingcompany.entity.AuthorityType;
import com.hlbrc.movingcompany.entity.AuthorityTypeExample;
import com.hlbrc.movingcompany.entity.Manager;
import com.hlbrc.movingcompany.entity.ManagerExample;
import com.hlbrc.movingcompany.entity.RoleAndRule;
import com.hlbrc.movingcompany.entity.RoleAndRuleExample;
import com.hlbrc.movingcompany.entity.RoleManager;
import com.hlbrc.movingcompany.entity.RoleManagerExample;
import com.hlbrc.movingcompany.enums.IMyEnums;
import com.hlbrc.movingcompany.mapper.IAuthorityManagerMapper;
import com.hlbrc.movingcompany.mapper.IAuthorityTypeMapper;
import com.hlbrc.movingcompany.mapper.IManagerMapper;
import com.hlbrc.movingcompany.mapper.IRoleAndRuleMapper;
import com.hlbrc.movingcompany.mapper.IRoleManagerMapper;
import com.hlbrc.movingcompany.service.IManagerService;
import com.hlbrc.movingcompany.util.MD5;
import com.hlbrc.movingcompany.util.MyToos;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service("ManagerServiceImpl")
public class ManagerServiceImpl implements IManagerService {

	@Autowired
	private IManagerMapper manager_mapper;
	@Autowired
	private IRoleManagerMapper role_manager_mapper;
	@Autowired
	private IAuthorityTypeMapper authority_type_mapper;
	@Autowired
	private IAuthorityManagerMapper authority_manager_mapper;
	@Autowired
	private IRoleAndRuleMapper role_and_rule_mapper;
	
	@Override
	public String managerlogin(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		if(message!=null&&!"".equals(message)) {
			ManagerExample example = new ManagerExample();
			ManagerExample.Criteria criteria = example.createCriteria();
			json = JSONObject.fromObject(message);
			criteria.andNameEqualTo(json.getString("username"));
			criteria.andPasswordEqualTo(MD5.getMD5(json.getString("password")));
			List<Manager> list = manager_mapper.selectByExample(example);
			if(list!=null&&list.size()>0) {
				session.setAttribute("manager", list.get(0));
				obj.put("manager", list.get(0));
				obj.put("msg", IMyEnums.SUCCEED);
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String queryManager(String message) throws ParseException {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		if(message!=null&&!"".equals(message)) {
			ManagerExample example = new ManagerExample();
			ManagerExample.Criteria criteria = example.createCriteria();
			json = JSONObject.fromObject(message);
			String format = "yyyy-MM-dd";
			if(!"".equals(json.getString("username"))) {
	        	criteria.andNameLike("%"+json.getString("username")+"%");
	        }
			if(!"".equals(json.getString("startTime"))&&!"".equals(json.getString("endTime"))) {
				criteria.andCreatetimeBetween(new SimpleDateFormat(format).parse(json.getString("startTime")),new SimpleDateFormat(format).parse(json.getString("endTime")));
			}
			else if(!"".equals(json.getString("startTime"))&&"".equals(json.getString("endTime"))) {
				criteria.andCreatetimeGreaterThanOrEqualTo(new SimpleDateFormat(format).parse(json.getString("startTime")));
			}
			else if("".equals(json.getString("startTime"))&&!"".equals(json.getString("endTime"))) {
				criteria.andCreatetimeLessThanOrEqualTo(new SimpleDateFormat(format).parse(json.getString("endTime")));
			}
			criteria.andStatusGreaterThan(IMyEnums.DELETE);
			example.setOrderByClause("createTime ASC");
			long all = manager_mapper.countByExample(example);
			if(all>0) {
				int index = json.getInt("pageIndex");
				int star = 0;
				int psize = 6;
				obj.put("allNumber", all);
				if (all % psize == 0) {
					all = all / psize;
				} else {
					all = all / psize + 1;
				}
				if (index > 1) {
					star = (index - 1) * psize;
				}
				example.setPageIndex(star);
		        example.setPageSize(psize);
			}
			
			List<Manager> list = manager_mapper.selectByExample(example);
			if(list!=null&&list.size()>0) {
				obj.put("msg", IMyEnums.SUCCEED);
				for(Manager m:list) {
					RoleAndRuleExample rexample = new RoleAndRuleExample();
					RoleAndRuleExample.Criteria rcriteria = rexample.createCriteria();
					rcriteria.andManageridEqualTo(m.getManagerid());
					rcriteria.andRoleandrulestatusGreaterThan(IMyEnums.DELETE);
					List<RoleAndRule> rlist = role_and_rule_mapper.selectByExample(rexample);
					String rose = "";
					int i=0;
					if(rlist!=null&&rlist.size()>0) {
						for(RoleAndRule r:rlist)
						{
							rose += role_manager_mapper.selectByPrimaryKey(r.getRolemanagerid()).getRolename();
							if(i<rlist.size()-1) {
								rose +=",";
							}
							i++;
						}
					}
					else {
						rose = "暂无角色";
					}
					m.setRose(rose);
				}
				obj.put("allPageNumber", all);
				JSONArray jsonarray = JSONArray.fromObject(list);
				obj.put("jsonarray", jsonarray);
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String queryrole(String message,Integer psize) throws ParseException {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		if(message!=null&&!"".equals(message)) {
			RoleManagerExample example = new RoleManagerExample();
			RoleManagerExample.Criteria criteria = example.createCriteria();
			json = JSONObject.fromObject(message);
			String format = "yyyy-MM-dd";
	        if(!"".equals(json.getString("rolename"))) {
	        	criteria.andRolenameLike("%"+json.getString("rolename")+"%");
	        }
			if(!"".equals(json.getString("startTime"))&&!"".equals(json.getString("endTime"))) {
				criteria.andCreatetimeBetween(new SimpleDateFormat(format).parse(json.getString("startTime")),new SimpleDateFormat(format).parse(json.getString("endTime")));
			}
			else if(!"".equals(json.getString("startTime"))&&"".equals(json.getString("endTime"))) {
				criteria.andCreatetimeGreaterThanOrEqualTo(new SimpleDateFormat(format).parse(json.getString("startTime")));
			}
			else if("".equals(json.getString("startTime"))&&!"".equals(json.getString("endTime"))) {
				criteria.andCreatetimeLessThanOrEqualTo(new SimpleDateFormat(format).parse(json.getString("endTime")));
			}
			criteria.andRolestatusGreaterThan(IMyEnums.DELETE);
			example.setOrderByClause("createTime ASC");
			long all = role_manager_mapper.countByExample(example);
			if(all>0&&psize!=null) {
				int index = json.getInt("pageIndex");
				int star = 0;
				obj.put("allNumber", all);
				if (all % psize == 0) {
					all = all / psize;
				} else {
					all = all / psize + 1;
				}
				if (index > 1) {
					star = (index - 1) * psize;
				}
				example.setPageIndex(star);
		        example.setPageSize(psize);
			}
	        List<RoleManager> list = role_manager_mapper.selectByExample(example);
	        obj.put("allPageNumber", all);
	        if(list!=null&&list.size()>0) {
	        	for(RoleManager r:list) {
	        		r.setCreatorname(manager_mapper.selectByPrimaryKey(r.getCreator()).getName());
	        		if(r.getModifier()!=null&&!"".equals(r.getModifier()+"")) {
	        			r.setModifiername(manager_mapper.selectByPrimaryKey(r.getModifier()).getName());
	        		}
	        		RoleAndRuleExample example2 = new RoleAndRuleExample();
	        		RoleAndRuleExample.Criteria criteria2 = example2.createCriteria();
	        		criteria2.andRolemanageridEqualTo(r.getRolemanagerid());
	        		criteria2.andManageridIsNull();
	        		criteria2.andRoleandrulestatusGreaterThan(IMyEnums.DELETE);
	        		List<RoleAndRule> list2 = role_and_rule_mapper.selectByExample(example2);
	        		String str = "";
	        		if(list2!=null&&list2.size()>0) {
	        			int i=0;
	        			for(RoleAndRule rr:list2) {
	        				str+=authority_manager_mapper.selectByPrimaryKey(rr.getAuthoritymanagerid()).getAuthorityname();
	        				if(i<list2.size()-1) {
	        					str+=",";
	        				}
	        				i++;
	        			}
	        		}
	        		r.setAuthoritynames(str);
	        	}
	        	obj.put("rolelist", list);
				obj.put("msg", IMyEnums.SUCCEED);
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String queryauthoritytype(String message,Integer psize) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		if(message!=null&&!"".equals(message)) {
			AuthorityTypeExample example = new AuthorityTypeExample();
			AuthorityTypeExample.Criteria criteria = example.createCriteria();
			json = JSONObject.fromObject(message);
			if(!"".equals(json.getString("authoritytypename"))) {
	        	criteria.andAuthoritynameLike("%"+json.getString("authoritytypename")+"%");
	        }
			criteria.andAuthoritytypestatusGreaterThan(IMyEnums.DELETE);
			example.setOrderByClause("createTime ASC");
			long all = authority_type_mapper.countByExample(example);
			if(all>0) {
				int index = json.getInt("pageIndex");
				if(psize!=null) {
					int star = 0;
					obj.put("allNumber", all);
					if (all % psize == 0) {
						all = all / psize;
					} else {
						all = all / psize + 1;
					}
					if (index > 1) {
						star = (index - 1) * psize;
					}
					example.setPageIndex(star);
			        example.setPageSize(psize);
				}
			}
	        List<AuthorityType> list = authority_type_mapper.selectByExample(example);
	        obj.put("allPageNumber", all);
	        if(list!=null&&list.size()>0) {
	        	for(int i=0;i<list.size();i++) {
	        		list.get(i).setCreatorname(manager_mapper.selectByPrimaryKey(list.get(i).getCreator()).getName());
	        		if(list.get(i).getModifier()!=null&&!"".equals(list.get(i).getModifier()+"")) {
	        			list.get(i).setModifiername(manager_mapper.selectByPrimaryKey(list.get(i).getModifier()).getName());
	        		}
	        		AuthorityManagerExample example2 = new AuthorityManagerExample();
	        		AuthorityManagerExample.Criteria criteria2 = example2.createCriteria();
	        		criteria2.andAuthoritytypeidEqualTo(list.get(i).getAuthoritytypeid());
	        		criteria2.andAuthoritymanagerstatusGreaterThan(IMyEnums.DELETE);
	        		List<AuthorityManager> list2 = authority_manager_mapper.selectByExample(example2);
	        		list.get(i).setAuthorityManager(list2);
	        	}
	        	obj.put("authoritytype", list);
				obj.put("msg", IMyEnums.SUCCEED);
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String queryauthority(String message,Integer psize) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		if(message!=null&&!"".equals(message)) {
			AuthorityManagerExample example = new AuthorityManagerExample();
			AuthorityManagerExample.Criteria criteria = example.createCriteria();
			json = JSONObject.fromObject(message);
	        if(!"".equals(json.getString("authorityname"))) {
	        	criteria.andAuthoritynameLike("%"+json.getString("authorityname")+"%");
	        }
	        if(!"".equals(json.getString("authorityrule"))) {
	        	criteria.andAuthoritynameLike("%"+json.getString("authorityrule")+"%");
	        }
	        criteria.andAuthoritymanagerstatusGreaterThan(IMyEnums.DELETE);
	        example.setOrderByClause("createTime ASC");
			long all = authority_manager_mapper.countByExample(example);
			if(all>0) {
				int index = json.getInt("pageIndex");
				if(psize!=null) {
					int star = 0;
					obj.put("allNumber", all);
					if (all % psize == 0) {
						all = all / psize;
					} else {
						all = all / psize + 1;
					}
					if (index > 1) {
						star = (index - 1) * psize;
					}
					example.setPageIndex(star);
			        example.setPageSize(psize);
				}
			}
	        List<AuthorityManager> list = authority_manager_mapper.selectByExample(example);
	        obj.put("allPageNumber", all);
	        if(list!=null&&list.size()>0) {
	        	for(int i=0;i<list.size();i++) {
	        		list.get(i).setAuthoritytypename(authority_type_mapper.selectByPrimaryKey(list.get(i).getAuthoritytypeid()).getAuthorityname());
	        		list.get(i).setCreatorname(manager_mapper.selectByPrimaryKey(list.get(i).getCreator()).getName());
	        		if(list.get(i).getModifier()!=null&&!"".equals(list.get(i).getModifier()+"")) {
	        			list.get(i).setModifiername(manager_mapper.selectByPrimaryKey(list.get(i).getModifier()).getName());
	        		}
	        	}
	        	obj.put("authority", list);
				obj.put("msg", IMyEnums.SUCCEED);
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String queryroleandrule(String message) throws ParseException {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		if(message!=null&&!"".equals(message)) {
			RoleAndRuleExample example = new RoleAndRuleExample();
			RoleAndRuleExample.Criteria criteria = example.createCriteria();
			json = JSONObject.fromObject(message);
			String format = "yyyy-MM-dd";
			example.setOrderByClause("createTime ASC");
			example.setPageIndex(Integer.parseInt(json.getString("pageIndex"))-1);
	        example.setPageSize(2);
	        if(!"".equals(json.getString("authoritymanagerid"))) {
	        	criteria.andAuthoritymanageridEqualTo(Integer.parseInt(json.getString("authoritymanagerid")));
	        }
	        if(!"".equals(json.getString("rolemanagerid"))) {
	        	criteria.andRolemanageridEqualTo(Integer.parseInt(json.getString("rolemanagerid")));
	        }
			if(!"".equals(json.getString("managerid"))) {
				criteria.andManageridEqualTo(Integer.parseInt(json.getString("managerid")));
			}
			if(!"".equals(json.getString("roleandruleid"))) {
				criteria.andRoleandruleidEqualTo(Integer.parseInt(json.getString("roleandruleid")));
			}
			if(!"".equals(json.getString("roleandrulestatus"))) {
				criteria.andRoleandrulestatusEqualTo(json.getString("roleandrulestatus"));
			}
			if(!"".equals(json.getString("startTime"))&&!"".equals(json.getString("endTime"))) {
				criteria.andCreatetimeBetween(new SimpleDateFormat(format).parse(json.getString("startTime")),new SimpleDateFormat(format).parse(json.getString("endTime")));
			}
			else if(!"".equals(json.getString("startTime"))&&"".equals(json.getString("endTime"))) {
				criteria.andCreatetimeGreaterThanOrEqualTo(new SimpleDateFormat(format).parse(json.getString("startTime")));
			}
			else if("".equals(json.getString("startTime"))&&!"".equals(json.getString("endTime"))) {
				criteria.andCreatetimeLessThanOrEqualTo(new SimpleDateFormat(format).parse(json.getString("endTime")));
			}
	        List<RoleAndRule> list = role_and_rule_mapper.selectByExample(example);
	        if(list!=null&&list.size()>0) {
	        	obj.put("authoritymanager", list.get(0));
				obj.put("msg", IMyEnums.SUCCEED);
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String insertManager(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager old_manager = (Manager) session.getAttribute("manager");
		if(old_manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				Manager manager = new Manager();
				manager.setCreatetime(new Date());
				manager.setCreator(old_manager.getManagerid());
				manager.setName(json.getString("name"));
				manager.setPassword(MD5.getMD5(json.getString("password")));
				manager.setStatus(IMyEnums.USER_NORMAL);
				ManagerExample example = new ManagerExample();
				ManagerExample.Criteria  criteria = example.createCriteria();
				criteria.andTelEqualTo(json.getString("tel"));
				List<Manager> list = manager_mapper.selectByExample(example);
				if(list!=null&&list.size()>0) {
					obj.put("msg", IMyEnums.TEL_ALREADY_EXISTS);
					return obj.toString();
				}
				manager.setTel(json.getString("tel"));
				manager.setEmail(json.getString("email"));
				int i = manager_mapper.insertSelective(manager);
				int id = 0;
				if(i>0) {
					example = new ManagerExample();
					criteria = example.createCriteria();
					criteria.andTelEqualTo(json.getString("tel"));
					id = manager_mapper.selectByExample(example).get(0).getManagerid();
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
					return obj.toString();
				}
				String[] rolemanagerids = null;
				if(!"".equals(json.getString("rolemanagerids"))) {
					rolemanagerids = json.getString("rolemanagerids").split(";");
					for(String s:rolemanagerids) {
						RoleAndRule roleandrule = new RoleAndRule();
						roleandrule.setCreatetime(new Date());
						roleandrule.setCreator(old_manager.getManagerid());
						roleandrule.setManagerid(id);
						roleandrule.setRoleandrulestatus(IMyEnums.NORMAL);
						roleandrule.setRolemanagerid(Integer.parseInt(s));
						i += role_and_rule_mapper.insertSelective(roleandrule);
					}
				}
				if(i>=rolemanagerids.length) {
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String insertrole(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				RoleManager rose = new RoleManager();
				rose.setCreatetime(new Date());
				rose.setCreator(manager.getManagerid());
				rose.setRoledescribe(json.getString("roledescribe"));
				RoleManagerExample example = new RoleManagerExample();
				RoleManagerExample.Criteria  criteria = example.createCriteria();
				criteria.andRolenameEqualTo(json.getString("rolename"));
				List<RoleManager> list = role_manager_mapper.selectByExample(example);
				if(list!=null&&list.size()>0) {
					obj.put("msg", IMyEnums.ROSE_NAME_ALREADY_EXISTS);
					return obj.toString();
				}
				rose.setRolename(json.getString("rolename"));
				rose.setRolestatus(IMyEnums.NORMAL);
				String[] authoritymanagerids = null;
				int id = 0;
				int i = role_manager_mapper.insertSelective(rose);
				if(i>0) {
					example = new RoleManagerExample();
					criteria = example.createCriteria();
					criteria.andRolenameEqualTo(json.getString("rolename"));
					id = role_manager_mapper.selectByExample(example).get(0).getRolemanagerid();
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
					return obj.toString();
				}
				if(!"".equals(json.getString("authoritymanagerids"))) {
					authoritymanagerids = json.getString("authoritymanagerids").split(";");
					for(String a:authoritymanagerids) {
						RoleAndRule roleandrule = new RoleAndRule();
						roleandrule.setCreatetime(new Date());
						roleandrule.setCreator(manager.getManagerid());
						roleandrule.setRoleandrulestatus(IMyEnums.NORMAL);
						roleandrule.setAuthoritymanagerid(Integer.parseInt(a));
						roleandrule.setRolemanagerid(id);
						i += role_and_rule_mapper.insertSelective(roleandrule);
					}
				}
				if(i>=authoritymanagerids.length) {
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String insertauthoritytype(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				AuthorityType authoritype = new AuthorityType();
				AuthorityTypeExample example = new AuthorityTypeExample();
				AuthorityTypeExample.Criteria criteria = example.createCriteria();
				criteria.andAuthoritynameEqualTo(json.getString("authorityname"));
				List<AuthorityType> list = authority_type_mapper.selectByExample(example);
				if(list!=null&&list.size()>0) {
					obj.put("msg", IMyEnums.AUTHORITY_TYPE_NAME_ALREADY_EXISTS);
					return obj.toString();
				}
				authoritype.setAuthorityname(json.getString("authorityname"));
				authoritype.setCreatetime(new Date());
				authoritype.setCreator(manager.getManagerid());
				authoritype.setAuthoritytypestatus(IMyEnums.NORMAL);
				int i = authority_type_mapper.insertSelective(authoritype);
				if(i>0) {
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String insertauthority(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				AuthorityManager authoritymanager = new AuthorityManager();
				AuthorityManagerExample example = new AuthorityManagerExample();
				AuthorityManagerExample.Criteria criteria = example.createCriteria();
				if(json.getString("authorityrule")!=null&&!"".equals(json.getString("authorityrule"))) {
					criteria.andAuthorityruleEqualTo(json.getString("authorityrule"));
					List<AuthorityManager> list = authority_manager_mapper.selectByExample(example);
					if(list!=null&&list.size()>0) {
						obj.put("msg", IMyEnums.AUTHORITY_ALREADY_EXISTS);
						return obj.toString();
					}
					authoritymanager.setAuthorityrule(json.getString("authorityrule"));
				}
				if(json.getString("authorityname")!=null&&!"".equals(json.getString("authorityname"))) {
					example = new AuthorityManagerExample();
					criteria = example.createCriteria();
					criteria.andAuthoritynameEqualTo(json.getString("authorityname"));
					List<AuthorityManager> list = authority_manager_mapper.selectByExample(example);
					if(list!=null&&list.size()>0) {
						obj.put("msg", IMyEnums.AUTHORITY_NAME_ALREADY_EXISTS);
						return obj.toString();
					}
					authoritymanager.setAuthorityname(json.getString("authorityname"));
				}
				authoritymanager.setAuthoritytypeid(Integer.parseInt(json.getString("authoritytypeid")));
				authoritymanager.setCreatetime(new Date());
				authoritymanager.setCreator(manager.getManagerid());
				authoritymanager.setAuthoritymanagerstatus(IMyEnums.NORMAL);
				int i = authority_manager_mapper.insertSelective(authoritymanager);
				if(i>0) {
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String insertroleandrule(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				RoleAndRule roleandrule = new RoleAndRule();
				roleandrule.setCreatetime(new Date());
				roleandrule.setManagerid(manager.getManagerid());
				roleandrule.setRoleandruleid(Integer.parseInt(json.getString("roleandruleid")));
				roleandrule.setRoleandrulestatus(IMyEnums.NORMAL);
				roleandrule.setRolemanagerid(Integer.parseInt(json.getString("rolemanagerid")));
				int i = role_and_rule_mapper.insertSelective(roleandrule);
				if(i>0) {
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String updateManager(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager old_manager = (Manager) session.getAttribute("manager");
		if(old_manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				Manager manager = new Manager();
				ManagerExample example = new ManagerExample();
				ManagerExample.Criteria  criteria = example.createCriteria();
				manager.setUpdatetime(new Date());
				manager.setModifier(old_manager.getManagerid());
				if(json.getString("name")!=null&&!"".equals(json.getString("name"))) {
					manager.setName(json.getString("name"));
				}
				if(json.getString("password")!=null&&!"".equals(json.getString("password"))) {
					manager.setPassword(MD5.getMD5(json.getString("password")));
				}
				if(json.getString("tel")!=null&&!"".equals(json.getString("tel"))) {
					example = new ManagerExample();
					criteria.andTelEqualTo(json.getString("tel"));
					List<Manager> list = manager_mapper.selectByExample(example);
					if(list!=null&&list.size()>0) {
						obj.put("msg", IMyEnums.TEL_ALREADY_EXISTS);
						return obj.toString();
					}
					manager.setTel(json.getString("tel"));
				}
				if(json.getString("status")!=null&&!"".equals(json.getString("status"))) {
					manager.setStatus(json.getString("status"));
				}
				if(json.getString("email")!=null&&!"".equals(json.getString("email"))) {
					manager.setEmail(json.getString("email"));
				}
				example = new ManagerExample();
				criteria = example.createCriteria();
				criteria.andManageridEqualTo(Integer.parseInt(json.getString("managerid")));
				int i = manager_mapper.updateByExampleSelective(manager, example);
				String[] rolemanagerids = null;
				if(json.getString("rolemanagerids")!=null&&!"".equals(json.getString("rolemanagerids"))) {
					RoleAndRuleExample example1 = new RoleAndRuleExample();
					RoleAndRuleExample.Criteria criteria1 = example1.createCriteria();
					criteria1.andManageridEqualTo(Integer.parseInt(json.getString("managerid")));
					criteria1.andRoleandrulestatusGreaterThan(IMyEnums.DELETE);
					List<RoleAndRule> list = role_and_rule_mapper.selectByExample(example1);
					rolemanagerids = json.getString("rolemanagerids").split(";");
					String[] orgList = null;
					if(list!=null&&list.size()>0&&rolemanagerids!=null&&rolemanagerids.length>0) {
						orgList = new String[list.size()];
						for(int j=0;j<list.size();j++) {
							orgList[j] = list.get(j).getRolemanagerid()+"";
						}
						MyToos mytoos = new MyToos();
						Map<String,Set<String>> map = mytoos.Bj(orgList, rolemanagerids);//判断数据状态 新增 修改 删除
						for(String s:map.get("1")) {
							RoleAndRule roleandrule = new RoleAndRule();
							roleandrule.setCreatetime(new Date());
							roleandrule.setCreator(old_manager.getManagerid());
							roleandrule.setManagerid(Integer.parseInt(json.getString("managerid")));
							roleandrule.setRoleandrulestatus(IMyEnums.NORMAL);
							roleandrule.setRolemanagerid(Integer.parseInt(s));
							i += role_and_rule_mapper.insertSelective(roleandrule);
						}
						for(String s:map.get("-1")) {
							RoleAndRuleExample example2 = new RoleAndRuleExample();
							RoleAndRuleExample.Criteria criteria2 = example2.createCriteria();
							criteria2.andManageridEqualTo(Integer.parseInt(json.getString("managerid")));
							criteria2.andRolemanageridEqualTo(Integer.parseInt(s));
							RoleAndRule rar = new RoleAndRule();
							rar.setRoleandrulestatus(IMyEnums.DELETE);
							rar.setModifier(old_manager.getManagerid());
							rar.setUpdatetime(new Date());
							i += role_and_rule_mapper.updateByExampleSelective(rar, example2);
						}
					}
					else if(list!=null&&list.size()<=0&&rolemanagerids!=null&&rolemanagerids.length>0){
						for(String s:rolemanagerids) {
							RoleAndRule roleandrule = new RoleAndRule();
							roleandrule.setCreatetime(new Date());
							roleandrule.setCreator(old_manager.getManagerid());
							roleandrule.setManagerid(Integer.parseInt(json.getString("managerid")));
							roleandrule.setRoleandrulestatus(IMyEnums.NORMAL);
							roleandrule.setRolemanagerid(Integer.parseInt(s));
							i += role_and_rule_mapper.insertSelective(roleandrule);
						}
					}
					else{
						if(list!=null&&list.size()>0) {
							for(int j=0;j<list.size();j++) {
								RoleAndRuleExample example2 = new RoleAndRuleExample();
								RoleAndRuleExample.Criteria criteria2 = example1.createCriteria();
								criteria2.andManageridEqualTo(Integer.parseInt(json.getString("managerid")));
								criteria2.andRolemanageridEqualTo(list.get(j).getRolemanagerid());
								RoleAndRule rar = new RoleAndRule();
								rar.setRoleandrulestatus(IMyEnums.DELETE);
								rar.setModifier(old_manager.getManagerid());
								rar.setUpdatetime(new Date());
								i += role_and_rule_mapper.updateByExampleSelective(rar, example2);
							}
						}
					}
				}
				if(i>0) {
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String updaterole(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager old_manager = (Manager) session.getAttribute("manager");
		if(old_manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				RoleManager rolemanager = new RoleManager();
				rolemanager.setModifier(old_manager.getManagerid());
				rolemanager.setUpdatetime(new Date());
				if(json.getString("roledescribe")!=null&&!"".equals(json.getString("roledescribe"))) {
					rolemanager.setRoledescribe(json.getString("roledescribe"));
				}
				if(json.getString("rolestatus")!=null&&!"".equals(json.getString("rolestatus"))) {
					rolemanager.setRolestatus(json.getString("rolestatus"));
				}
				RoleManagerExample example = new RoleManagerExample();
				RoleManagerExample.Criteria  criteria = example.createCriteria();
				if(json.getString("rolename")!=null&&!"".equals(json.getString("rolename"))) {
					criteria.andRolenameEqualTo(json.getString("rolename"));
					List<RoleManager> list = role_manager_mapper.selectByExample(example);
					if(list!=null&&list.size()>0) {
						obj.put("msg", IMyEnums.ROSE_NAME_ALREADY_EXISTS);
						return obj.toString();
					}
					rolemanager.setRolename(json.getString("rolename"));
				}
				example = new RoleManagerExample();
				criteria = example.createCriteria();
				criteria.andRolemanageridEqualTo(Integer.parseInt(json.getString("rolemanagerid")));
				int i = role_manager_mapper.updateByExampleSelective(rolemanager, example);
				String[] authoritymanagerids = null;
				if(json.getString("authoritymanagerids")!=null&&!"".equals(json.getString("authoritymanagerids"))) {
					RoleAndRuleExample example1 = new RoleAndRuleExample();
					RoleAndRuleExample.Criteria criteria1 = example1.createCriteria();
					criteria1.andRolemanageridEqualTo(Integer.parseInt(json.getString("rolemanagerid")));
					criteria1.andRoleandrulestatusGreaterThan(IMyEnums.DELETE);
					List<RoleAndRule> list = role_and_rule_mapper.selectByExample(example1);
					authoritymanagerids = json.getString("authoritymanagerids").split(";");
					String[] orgList = null;
					if(list!=null&&list.size()>0&&authoritymanagerids.length>0) {
						orgList = new String[list.size()];
						for(int j=0;j<list.size();j++) {
							orgList[j] = list.get(j).getAuthoritymanagerid()+"";
						}
						MyToos mytoos = new MyToos();
						Map<String,Set<String>> map = mytoos.Bj(orgList, authoritymanagerids);
						for(String s:map.get("1")) {
							RoleAndRule roleandrule = new RoleAndRule();
							roleandrule.setCreatetime(new Date());
							roleandrule.setCreator(old_manager.getManagerid());
							roleandrule.setRolemanagerid(Integer.parseInt(json.getString("rolemanagerid")));
							roleandrule.setRoleandrulestatus(IMyEnums.NORMAL);
							roleandrule.setAuthoritymanagerid(Integer.parseInt(s));
							i += role_and_rule_mapper.insertSelective(roleandrule);
						}
						for(String s:map.get("-1")) {
							RoleAndRuleExample example2 = new RoleAndRuleExample();
							RoleAndRuleExample.Criteria criteria2 = example2.createCriteria();
							criteria2.andRolemanageridEqualTo(Integer.parseInt(json.getString("rolemanagerid")));
							criteria2.andAuthoritymanageridEqualTo(Integer.parseInt(s));
							RoleAndRule rar = new RoleAndRule();
							rar.setRoleandrulestatus(IMyEnums.DELETE);
							rar.setModifier(old_manager.getManagerid());
							rar.setUpdatetime(new Date());
							i += role_and_rule_mapper.updateByExampleSelective(rar, example2);
						}
					}
					else if(list!=null&&list.size()<=0&&authoritymanagerids!=null&&authoritymanagerids.length>0){
						for(String s:authoritymanagerids) {
							RoleAndRule roleandrule = new RoleAndRule();
							roleandrule.setCreatetime(new Date());
							roleandrule.setCreator(old_manager.getManagerid());
							roleandrule.setRolemanagerid(Integer.parseInt(json.getString("rolemanagerid")));
							roleandrule.setRoleandrulestatus(IMyEnums.NORMAL);
							roleandrule.setAuthoritymanagerid(Integer.parseInt(s));
							i += role_and_rule_mapper.insertSelective(roleandrule);
						}
					}
					else{
						if(authoritymanagerids!=null&&authoritymanagerids.length<=0) {
							RoleAndRuleExample example2 = new RoleAndRuleExample();
							RoleAndRuleExample.Criteria criteria2 = example1.createCriteria();
							for(int j=0;j<list.size();j++) {
								criteria2.andRolemanageridEqualTo(Integer.parseInt(json.getString("rolemanagerid")));
								criteria2.andAuthoritymanageridEqualTo(list.get(j).getAuthoritymanagerid());
								RoleAndRule rar = new RoleAndRule();
								rar.setRoleandrulestatus(IMyEnums.DELETE);
								rar.setModifier(old_manager.getManagerid());
								rar.setUpdatetime(new Date());
								i += role_and_rule_mapper.updateByExampleSelective(rar, example2);
							}
						}
					}
				}
				if(i>0) {
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String updateauthoritytype(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				AuthorityType authoritytype = new AuthorityType();
				if(json.getString("authorityname")!=null&&!"".equals(json.getString("authorityname"))) {
					AuthorityTypeExample example = new AuthorityTypeExample();
					AuthorityTypeExample.Criteria criteria = example.createCriteria();
					criteria.andAuthoritynameEqualTo(json.getString("authorityname"));
					List<AuthorityType> list = authority_type_mapper.selectByExample(example);
					if(list!=null&&list.size()>0) {
						obj.put("msg", IMyEnums.AUTHORITY_TYPE_NAME_ALREADY_EXISTS);
						return obj.toString();
					}
					authoritytype.setAuthorityname(json.getString("authorityname"));
				}
				authoritytype.setModifier(manager.getManagerid());
				authoritytype.setUpdatetime(new Date());
				if(json.getString("authoritytypestatus")!=null&&!"".equals(json.getString("authoritytypestatus"))) {
					authoritytype.setAuthoritytypestatus(json.getString("authoritytypestatus"));
				}
				AuthorityTypeExample example = new AuthorityTypeExample();
				AuthorityTypeExample.Criteria criteria = example.createCriteria();
				criteria.andAuthoritytypeidEqualTo(Integer.parseInt(json.getString("authoritytypeid")));
				int i = authority_type_mapper.updateByExampleSelective(authoritytype, example);
				if(i>0) {
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String updateauthority(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				AuthorityManagerExample example = new AuthorityManagerExample();
				AuthorityManagerExample.Criteria criteria = example.createCriteria();
				AuthorityManager authoritymanager = new AuthorityManager();
				if(json.getString("authorityrule")!=null&&!"".equals(json.getString("authorityrule"))) {
					criteria.andAuthorityruleEqualTo(json.getString("authorityrule"));
					List<AuthorityManager> list = authority_manager_mapper.selectByExample(example);
					if(list!=null&&list.size()>0) {
						obj.put("msg", IMyEnums.AUTHORITY_ALREADY_EXISTS);
						return obj.toString();
					}
					authoritymanager.setAuthorityrule(json.getString("authorityrule"));
				}
				if(json.getString("authorityname")!=null&&!"".equals(json.getString("authorityname"))) {
					example = new AuthorityManagerExample();
					criteria = example.createCriteria();
					criteria.andAuthoritynameEqualTo(json.getString("authorityname"));
					List<AuthorityManager> list = authority_manager_mapper.selectByExample(example);
					if(list!=null&&list.size()>0) {
						obj.put("msg", IMyEnums.AUTHORITY_NAME_ALREADY_EXISTS);
						return obj.toString();
					}
					authoritymanager.setAuthorityname(json.getString("authorityname"));
				}
				if(json.getString("authoritytypeid")!=null&&!"".equals(json.getString("authoritytypeid"))) {
					authoritymanager.setAuthoritytypeid(Integer.parseInt(json.getString("authoritytypeid")));
				}
				example = new AuthorityManagerExample();
				criteria = example.createCriteria();
				criteria.andAuthoritymanageridEqualTo(json.getInt("authoritymanagerid"));
				authoritymanager.setModifier(manager.getManagerid());
				authoritymanager.setUpdatetime(new Date());
				if(json.getString("authoritymanagerstatus")!=null&&!"".equals(json.getString("authoritymanagerstatus"))) {
					authoritymanager.setAuthoritymanagerstatus(json.getString("authoritymanagerstatus"));
				}
				int i = authority_manager_mapper.updateByExampleSelective(authoritymanager, example);
				if(i>0) {
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String updateroleandrule(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		@SuppressWarnings("unused")
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String setManagerstatus(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager_old = (Manager) session.getAttribute("manager");
		if(manager_old!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				if(json.getString("status")!=null&&!"".equals(json.getString("status"))) {
					ManagerExample example  = new ManagerExample();
					ManagerExample.Criteria criteria = example.createCriteria();
					criteria.andManageridEqualTo(Integer.parseInt(json.getString("managerid")));
					Manager manager = new Manager();
					manager.setStatus(json.getString("status"));
					manager.setModifier(manager_old.getManagerid());
					manager.setUpdatetime(new Date());
					int i = manager_mapper.updateByExampleSelective(manager, example);
					if(i>0) {
						obj.put("msg", IMyEnums.SUCCEED);
					}
					else {
						obj.put("msg", IMyEnums.FAIL);
					}
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String setrolestatus(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				if(json.getString("rolestatus")!=null&&!"".equals(json.getString("rolestatus"))) {
					RoleManagerExample example  = new RoleManagerExample();
					RoleManagerExample.Criteria criteria = example.createCriteria();
					criteria.andRolemanageridEqualTo(Integer.parseInt(json.getString("rolemanagerid")));
					RoleManager rolemanager = new RoleManager();
					rolemanager.setRolestatus(json.getString("rolestatus"));
					rolemanager.setModifier(manager.getManagerid());
					rolemanager.setUpdatetime(new Date());
					int i = role_manager_mapper.updateByExampleSelective(rolemanager, example);
					if(i>0) {
						obj.put("msg", IMyEnums.SUCCEED);
					}
					else {
						obj.put("msg", IMyEnums.FAIL);
					}
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String setauthoritytypestatus(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				if(json.getString("authoritytypestatus")!=null&&!"".equals(json.getString("authoritytypestatus"))) {
					AuthorityTypeExample example  = new AuthorityTypeExample();
					AuthorityTypeExample.Criteria criteria = example.createCriteria();
					criteria.andAuthoritytypeidEqualTo(Integer.parseInt(json.getString("authoritytypeid")));
					AuthorityType authoritytype = new AuthorityType();
					authoritytype.setAuthoritytypestatus(json.getString("authoritytypestatus"));
					authoritytype.setModifier(manager.getManagerid());
					authoritytype.setUpdatetime(new Date());
					int i = authority_type_mapper.updateByExampleSelective(authoritytype, example);
					if(i>0) {
						obj.put("msg", IMyEnums.SUCCEED);
					}
					else {
						obj.put("msg", IMyEnums.FAIL);
					}
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String setauthoritystatus(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				if(json.getString("authoritymanagerstatus")!=null&&!"".equals(json.getString("authoritymanagerstatus"))) {
					AuthorityManagerExample example  = new AuthorityManagerExample();
					AuthorityManagerExample.Criteria criteria = example.createCriteria();
					criteria.andAuthoritymanageridEqualTo(Integer.parseInt(json.getString("authoritymanagerid")));
					AuthorityManager authoritymanager = new AuthorityManager();
					authoritymanager.setAuthoritymanagerstatus(json.getString("authoritymanagerstatus"));
					authoritymanager.setModifier(manager.getManagerid());
					authoritymanager.setUpdatetime(new Date());
					int i = authority_manager_mapper.updateByExampleSelective(authoritymanager, example);
					if(i>0) {
						obj.put("msg", IMyEnums.SUCCEED);
					}
					else {
						obj.put("msg", IMyEnums.FAIL);
					}
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String setroleandrulestatus(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				if(json.getString("roleandrulestatus")!=null&&!"".equals(json.getString("roleandrulestatus"))) {
					RoleAndRuleExample example  = new RoleAndRuleExample();
					RoleAndRuleExample.Criteria criteria = example.createCriteria();
					criteria.andRoleandruleidEqualTo(Integer.parseInt(json.getString("roleandruleid")));
					RoleAndRule roleandrule = new RoleAndRule();
					roleandrule.setRoleandrulestatus(json.getString("roleandrulestatus"));
					roleandrule.setModifier(manager.getManagerid());
					roleandrule.setUpdatetime(new Date());
					int i = role_and_rule_mapper.updateByExampleSelective(roleandrule, example);
					if(i>0) {
						obj.put("msg", IMyEnums.SUCCEED);
					}
					else {
						obj.put("msg", IMyEnums.FAIL);
					}
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String setManagerliststatus(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager old_manager = (Manager) session.getAttribute("manager");
		if(old_manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				if(json.getString("status")!=null&&!"".equals(json.getString("status"))&&json.getString("managerids")!=null&&!"".equals(json.getString("managerids"))) {
					int i = 0;
					String[] managerids = json.getString("managerids").split(";");
					if(managerids!=null&&managerids.length>0) {
						for(int j=0;j<managerids.length;j++) {
							ManagerExample example  = new ManagerExample();
							ManagerExample.Criteria criteria = example.createCriteria();
							criteria.andManageridEqualTo(Integer.parseInt(managerids[j]));
							Manager manager = new Manager();
							manager.setStatus(json.getString("status"));
							manager.setModifier(old_manager.getManagerid());
							manager.setUpdatetime(new Date());
							i += manager_mapper.updateByExampleSelective(manager, example);
						}
					}
					if(i>0) {
						obj.put("msg", IMyEnums.SUCCEED);
					}
					else {
						obj.put("msg", IMyEnums.FAIL);
					}
					
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String setrolelistststus(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				if(json.getString("rolestatus")!=null&&!"".equals(json.getString("rolestatus"))&&json.getString("rolemanagerids")!=null&&!"".equals(json.getString("rolemanagerids"))) {
					String[] rolemanagerids = json.getString("rolemanagerids").split(";");
					int i = 0;
					if(rolemanagerids!=null&&rolemanagerids.length>0) {
						for(int j=0;j<rolemanagerids.length;j++) {
							RoleManagerExample example  = new RoleManagerExample();
							RoleManagerExample.Criteria criteria = example.createCriteria();
							criteria.andRolemanageridEqualTo(Integer.parseInt(rolemanagerids[j]));
							RoleManager rolemanager = new RoleManager();
							rolemanager.setRolestatus(json.getString("rolestatus"));
							rolemanager.setModifier(manager.getManagerid());
							rolemanager.setUpdatetime(new Date());
							i += role_manager_mapper.updateByExampleSelective(rolemanager, example);
						}
					}
					if(i>0) {
						obj.put("msg", IMyEnums.SUCCEED);
					}
					else {
						obj.put("msg", IMyEnums.FAIL);
					}
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String setauthoritytypeliststatus(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				if(json.getString("authoritytypestatus")!=null&&!"".equals(json.getString("authoritytypestatus"))&&json.getString("authoritytypeids")!=null&&!"".equals(json.getString("authoritytypeids"))) {
					String[] authoritytypeids = json.getString("authoritytypeids").split(";");
					int i = 0;
					if(authoritytypeids!=null&&authoritytypeids.length>0) {
						for(int j=0;j<authoritytypeids.length;j++) {
							AuthorityTypeExample example  = new AuthorityTypeExample();
							AuthorityTypeExample.Criteria criteria = example.createCriteria();
							criteria.andAuthoritytypeidEqualTo(Integer.parseInt(authoritytypeids[j]));
							AuthorityType authoritytype = new AuthorityType();
							authoritytype.setAuthoritytypestatus(json.getString("authoritytypestatus"));
							authoritytype.setModifier(manager.getManagerid());
							authoritytype.setUpdatetime(new Date());
							i += authority_type_mapper.updateByExampleSelective(authoritytype, example);
						}
					}
					
					if(i>0) {
						obj.put("msg", IMyEnums.SUCCEED);
					}
					else {
						obj.put("msg", IMyEnums.FAIL);
					}
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String setauthorityliststatus(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				if(json.getString("authoritymanagerstatus")!=null&&!"".equals(json.getString("authoritymanagerstatus"))&&json.getString("authoritymanagerids")!=null&&!"".equals(json.getString("authoritymanagerids"))) {
					String[] authoritymanagerids = json.getString("authoritymanagerids").split(";");
					int i = 0;
					if(authoritymanagerids!=null&&authoritymanagerids.length>0) {
						for(int j=0;j<authoritymanagerids.length;j++) {
							AuthorityManagerExample example  = new AuthorityManagerExample();
							AuthorityManagerExample.Criteria criteria = example.createCriteria();
							criteria.andAuthoritymanageridEqualTo(Integer.parseInt(authoritymanagerids[j]));
							AuthorityManager authoritymanager = new AuthorityManager();
							authoritymanager.setAuthoritymanagerstatus(json.getString("authoritymanagerstatus"));
							authoritymanager.setModifier(manager.getManagerid());
							authoritymanager.setUpdatetime(new Date());
							i += authority_manager_mapper.updateByExampleSelective(authoritymanager, example);
						}
					}
					if(i>0) {
						obj.put("msg", IMyEnums.SUCCEED);
					}
					else {
						obj.put("msg", IMyEnums.FAIL);
					}
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String setroleandruleliststatus(String message, HttpSession session) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		Manager manager = (Manager) session.getAttribute("manager");
		if(manager!=null) {
			if(message!=null&&!"".equals(message)) {
				json = JSONObject.fromObject(message);
				if(json.getString("roleandrulestatus")!=null&&!"".equals(json.getString("roleandrulestatus"))&&json.getString("roleandruleids")!=null&&!"".equals(json.getString("roleandruleids"))) {
					String[] roleandruleids = json.getString("roleandruleids").split(";");
					int i = 0;
					if(roleandruleids!=null&&roleandruleids.length>0) {
						for(int j=0;j<roleandruleids.length;j++) {
							RoleAndRuleExample example  = new RoleAndRuleExample();
							RoleAndRuleExample.Criteria criteria = example.createCriteria();
							criteria.andRoleandruleidEqualTo(Integer.parseInt(roleandruleids[j]));
							RoleAndRule roleandrule = new RoleAndRule();
							roleandrule.setRoleandrulestatus(json.getString("roleandrulestatus"));
							roleandrule.setModifier(manager.getManagerid());
							roleandrule.setUpdatetime(new Date());
							i += role_and_rule_mapper.updateByExampleSelective(roleandrule, example);
						}
					}
					if(i>0) {
						obj.put("msg", IMyEnums.SUCCEED);
					}
					else {
						obj.put("msg", IMyEnums.FAIL);
					}
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String queryManagerById(String message) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		if(message!=null&&!"".equals(message)) {
			json = JSONObject.fromObject(message);
			if(json.getString("managerid")!=null&&!"".equals(json.getString("managerid"))) {
				Manager manager = manager_mapper.selectByPrimaryKey(Integer.parseInt(json.getString("managerid")));
				if(manager!=null) {
					obj.put("manager", manager);
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String queryManagerByTel(String message) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		if(message!=null&&!"".equals(message)) {
			json = JSONObject.fromObject(message);
			if(json.getString("tel")!=null&&!"".equals(json.getString("tel"))) {
				ManagerExample example = new ManagerExample();
				ManagerExample.Criteria criteria = example.createCriteria();
				criteria.andTelEqualTo(json.getString("tel"));
				Manager manager = manager_mapper.selectByExample(example).get(0);
				if(manager!=null) {
					obj.put("manager", manager);
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String queryManagerByEmail(String message) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		if(message!=null&&!"".equals(message)) {
			json = JSONObject.fromObject(message);
			if(json.getString("email")!=null&&!"".equals(json.getString("email"))) {
				ManagerExample example = new ManagerExample();
				ManagerExample.Criteria criteria = example.createCriteria();
				criteria.andEmailEqualTo(json.getString("email"));
				Manager manager = manager_mapper.selectByExample(example).get(0);
				if(manager!=null) {
					obj.put("manager", manager);
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String queryroleById(String message) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		if(message!=null&&!"".equals(message)) {
			json = JSONObject.fromObject(message);
			if(json.getString("rolemanagerid")!=null&&!"".equals("rolemanagerid")){
				RoleManager rolemanager = role_manager_mapper.selectByPrimaryKey(Integer.parseInt(json.getString("rolemanagerid")));
				if(rolemanager!=null) {
					obj.put("rolemanager", rolemanager);
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String queryauthoritytypeById(String message) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		if(message!=null&&!"".equals(message)) {
			json = JSONObject.fromObject(message);
			if(json.getString("authoritytypeid")!=null&&!"".equals("authoritytypeid")){
				AuthorityType authoritytype = authority_type_mapper.selectByPrimaryKey(Integer.parseInt(json.getString("authoritytypeid")));
				if(authoritytype!=null) {
					obj.put("authoritytype", authoritytype);
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}

	@Override
	public String queryauthorityById(String message) {
		JSONObject obj = new JSONObject();
		JSONObject json = new JSONObject();
		if(message!=null&&!"".equals(message)) {
			json = JSONObject.fromObject(message);
			if(json.getString("authoritytypeid")!=null&&!"".equals("authoritytypeid")){
				AuthorityManager authoritymanager = authority_manager_mapper.selectByPrimaryKey(Integer.parseInt(json.getString("authoritytypeid")));
				if(authoritymanager!=null) {
					obj.put("authoritymanager", authoritymanager);
					obj.put("msg", IMyEnums.SUCCEED);
				}
				else {
					obj.put("msg", IMyEnums.FAIL);
				}
			}
			else {
				obj.put("msg", IMyEnums.FAIL);
			}
		}
		else {
			obj.put("msg", IMyEnums.FAIL);
		}
		return obj.toString();
	}
}
