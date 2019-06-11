package com.smartbossapp.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.smartbossapp.data.UserData;
import com.smartbossapp.model.User;
import com.smartbossapp.service.UserRegistration;

/**
 * JAX-RS Example
 * <p/>
 * This class produces a RESTful service to read/write the contents of the Users
 * table.
 */
@Path("/Users")
@RequestScoped
@Stateful
public class UserService {
	@Inject
	private Logger log;

	@Inject
	private Validator validator;

	@Inject
	private UserData userData;

	@Inject
	UserRegistration registration;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> listAllUsers() {
		return userData.findAllOrderedByFirstName();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces(MediaType.APPLICATION_JSON)
	public User lookupUserById(@PathParam("id") final long id) {
		final User User = userData.findById(id);
		if (User == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return User;
	}

	/**
	 * Creates a new User from the values provided. Performs validation, and will
	 * return a JAX-RS response with either 200 ok, or with a map of fields, and
	 * related errors.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUser(final User user) {

		Response.ResponseBuilder builder = null;

		try {
			// Validates User using bean validation
			validateUser(user);

			registration.register(user);

			// Create an "ok" response
			builder = Response.ok().entity(user);
		} catch (final ConstraintViolationException ce) {

			// Handle bean validation issues
			builder = createViolationResponse(ce.getConstraintViolations());

		} catch (final ValidationException e) {

			// Handle the unique constrain violation
			final Map<String, String> responseObj = new HashMap<String, String>();
			responseObj.put("email", "Email taken");
			builder = Response.status(Response.Status.CONFLICT).entity(responseObj);

		} catch (final Exception e) {
			// Handle generic exceptions
			final Map<String, String> responseObj = new HashMap<String, String>();
			responseObj.put("error", e.getMessage());
			builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
		}

		return builder.build();
	}

	/**
	 * <p>
	 * Validates the given User variable and throws validation exceptions based on
	 * the type of error. If the error is standard bean validation errors then it
	 * will throw a ConstraintValidationException with the set of the constraints
	 * violated.
	 * </p>
	 * <p>
	 * If the error is caused because an existing User with the same email is
	 * registered it throws a regular validation exception so that it can be
	 * interpreted separately.
	 * </p>
	 * 
	 * @param User User to be validated
	 * @throws ConstraintViolationException If Bean Validation errors exist
	 * @throws ValidationException          If User with the same email already
	 *                                      exists
	 */
	private void validateUser(final User User) throws ConstraintViolationException, ValidationException {
		// Create a bean validator and check for issues.
		final Set<ConstraintViolation<User>> violations = validator.validate(User);

		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
		}

		// Check the uniqueness of the email address
		if (emailAlreadyExists(User.getEmail())) {
			throw new ValidationException("Unique Email Violation");
		}
	}

	/**
	 * Creates a JAX-RS "Bad Request" response including a map of all violation
	 * fields, and their message. This can then be used by clients to show
	 * violations.
	 * 
	 * @param violations A set of violations that needs to be reported
	 * @return JAX-RS response containing all violations
	 */
	private Response.ResponseBuilder createViolationResponse(final Set<ConstraintViolation<?>> violations) {
		log.fine("Validation completed. violations found: " + violations.size());

		final Map<String, String> responseObj = new HashMap<String, String>();

		for (final ConstraintViolation<?> violation : violations) {
			responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
		}

		return Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
	}

	/**
	 * Checks if a User with the same email address is already registered. This is
	 * the only way to easily capture the "@UniqueConstraint(columnNames = "email")"
	 * constraint from the User class.
	 * 
	 * @param email The email to check
	 * @return True if the email already exists, and false otherwise
	 */
	public boolean emailAlreadyExists(final String email) {
		User User = null;
		try {
			User = userData.findByEmail(email);
		} catch (final NoResultException e) {
			// ignore
		}
		return User != null;
	}
}
