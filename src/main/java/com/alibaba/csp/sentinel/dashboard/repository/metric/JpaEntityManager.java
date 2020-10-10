package com.alibaba.csp.sentinel.dashboard.repository.metric;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ServiceInterfaceDetail;

@Transactional
@Component
public class JpaEntityManager  {

	public static final String QueryHint_Name ="org.hibernate.cacheable";
	public static final String QueryHint_GRAPH ="javax.persistence.loadgraph";
	public static final String QueryHint_Value ="true";
	@PersistenceContext
    private EntityManager em;
	
	
	public void save(Object t) {
		em.persist(t);
	}
	
	public <T> T createOrUpdate(T bean, Class<T> T) {
		return em.merge(bean);
	}
	
	public <T> List<T> findAll(Class<T> T) {
		return findByCondition(null, null, null, null, T);
	}
	
	public <T> T delete(Object id, Class<T> T) {
		T t = findById(id, T);
		if (null != t)
			em.remove(t);
		return t;
	}
	
	public <T> T findById(Object id, Class<T> T) {
		T t = em.find(T, id);
		return t;
	}
	
	public <T> T create(T bean, Class<T> T) {
		em.persist(bean);
		return bean;
	}
	
	/**
	 * @param <T>
	 * @return 
	 * 
	 */
	public <T>   boolean persistentBatch( List<ServiceInterfaceDetail> interfaceDetails)
			throws SQLException {
		 for (Object table : interfaceDetails){
	            em.persist(table);
	        }
	        em.flush();
	        em.clear();
			return true;
	}
	/**
	 * @param T
	 * @param whereClause
	 *            name=?1 and id=?2 or other=?3
	 * @param whereArgs
	 *            长度要与whereClause的问号对齐
	 */
	public <T> void delete(Class<T> T, String whereClause, Object... whereArgs) {
		String sql = "DELETE FROM " + T.getSimpleName();
		if (!StringUtils.isEmpty(whereClause)) {
			whereClause = formatSql4Jpa(whereClause);
			sql = sql + " WHERE " + whereClause + " ";
		}
		Query query = em.createQuery(sql);
		if (whereArgs != null && whereArgs.length > 0) {
			for (int i = 0; i < whereArgs.length; i++) {
				query.setParameter(i + 1, whereArgs[i]);
			}
		}
		query.executeUpdate();
	}
	
	/**
	 * 执行esql语句
	 * 
	 * @param esql
	 *            全语句
	 * @return
	 */
	public int executeUpdate(String esql) {
		return em.createQuery(esql).executeUpdate();
	}
	
	/**
	 * @param T
	 * @param whereClause
	 *            name=?1 and id=?2 or other=?3
	 * @param whereArgs
	 *            长度要与whereClause的问号对齐
	 * @return
	 */
	public <T> List<T> findByCondition(Class<T> T, String whereClause,
			Object... whereArgs) {
		return findByCondition(whereClause, whereArgs, null, null, T);
	}
	
	/**
	 * 多条件查询
	 * 
	 * @param whereClause
	 *            name=?1 and id=?2 or other=?3
	 * @param whereArgs
	 *            长度要与whereClause的问号对齐
	 * @param orderby 带order by 的 语句 例如： ‘order by id asc,name desc
	 * @param range
	 *            结果的范围，数组位置0为起始位置1为结束
	 * @param T
	 * @return
	 */
	public <T> List<T> findByCondition(String whereClause, Object[] whereArgs,
			String orderby, int[] range, Class<T> T) {

		return findByCondition(whereClause, whereArgs,
				orderby, range,false, T) ;
	}
	
	/**
	 * 多条件查询
	 * 
	 * @param whereClause
	 *            name=?1 and id=?2 or other=?3
	 * @param whereArgs
	 *            长度要与whereClause的问号对齐
	 * @param orderby 带order by 的 语句 例如： ‘order by id asc,name desc
	 * @param range
	 *            结果的范围，数组位置0为起始位置1为结束
	 * @param cacheable 是否缓存查询结果
	 * @param T
	 * @return
	 */
	public <T> List<T> findByCondition(String whereClause, Object[] whereArgs,
			String orderby, int[] range,boolean cacheable, Class<T> T) {
		return findByCondition(whereClause, whereArgs, orderby, range, cacheable, null, T);
	}
	
	/**
	 * 多条件查询
	 * 
	 * @param whereClause
	 *            name=?1 and id=?2 or other=?3
	 * @param whereArgs
	 *            长度要与whereClause的问号对齐
	 * @param orderby 带order by 的 语句 例如： ‘order by id asc,name desc
	 * @param range
	 *            结果的范围，数组位置0为起始位置1为结束
	 * @param cacheable 是否缓存查询结果
	 * @param entityGraphs 是否缓存查询结果@NamedEntityGraph 的名称，通过实体图的方式查询懒加载对象
	 * </br> NamedEntityGraph 可以在实体中设置
	 * @param T
	 * @return
	 */
	public <T> List<T> findByCondition(String whereClause, Object[] whereArgs,
			String orderby, int[] range,boolean cacheable,String[] entityGraphs, Class<T> T) {
		String sql = "FROM " + T.getSimpleName() + " ";
		if (!StringUtils.isEmpty(whereClause)) {
			whereClause = formatSql4Jpa(whereClause); 
			sql = sql + " WHERE " + whereClause + " ";
		}
		if (!StringUtils.isEmpty(orderby)) {
			sql = sql + orderby;
		}
		TypedQuery<T> query = em.createQuery(sql, T);
		if (whereArgs != null && whereArgs.length > 0) {
			for (int i = 0; i < whereArgs.length; i++) {
				query.setParameter(i + 1, whereArgs[i]);
			}
		}
		if (range != null && range.length == 2 && range[1] > range[0]) {
			query.setMaxResults(range[1] - range[0]);
			query.setFirstResult(range[0]);
		}
		if(cacheable) {
			query.setHint(QueryHint_Name, QueryHint_Value);
		}
		if (entityGraphs != null && entityGraphs.length > 0) {
			for (String eg : entityGraphs) {
				if(StringUtils.isNotBlank(eg)) {
					@SuppressWarnings("rawtypes")
					EntityGraph graph = this.em.getEntityGraph(eg);
					query.setHint(QueryHint_GRAPH, graph);
				}
			}
		}
		
		return query.getResultList();
	}

	private   String formatSql4Jpa(String sql){
		sql = sql+" ";
		int index = 1;
		String regex = "\\?(?=[^\\d])";
		Pattern p = Pattern.compile(regex);
	    Matcher m = p.matcher(sql); // 获取 matcher 对象
	    while(m.find()){
	    	 sql = sql.replaceFirst(regex, "?"+index+" ");
	    	 index++;
	    }
	    return sql;
	}
}
