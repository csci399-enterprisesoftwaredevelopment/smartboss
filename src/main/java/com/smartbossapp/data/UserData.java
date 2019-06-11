
package com.smartbossapp.data;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.smartbossapp.model.User;

@ApplicationScoped
public class UserData {

	@Inject
	private EntityManager em;

	public User findById(final Long id) {
		return em.find(User.class, id);
	}

	public User findByEmail(final String email) {
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<User> criteria = cb.createQuery(User.class);
		final Root<User> User = criteria.from(User.class);
		criteria.select(User).where(cb.equal(User.get("email"), email));
		return em.createQuery(criteria).getSingleResult();
	}

	public List<User> findAllOrderedByFirstName() {
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<User> criteria = cb.createQuery(User.class);
		final Root<User> User = criteria.from(User.class);
		criteria.select(User).orderBy(cb.asc(User.get("firstname")));
		return em.createQuery(criteria).getResultList();
	}
}
