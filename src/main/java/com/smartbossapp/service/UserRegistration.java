package com.smartbossapp.service;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.smartbossapp.model.User;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class UserRegistration {

	@Inject
	private Logger log;

	@Inject
	private EntityManager em;

	@Inject
	private Event<User> userEventSrc;

	public void register(final User user) throws Exception {
		log.info("Registering " + user.getFirstName());
		em.persist(user);
		userEventSrc.fire(user);
	}
}
