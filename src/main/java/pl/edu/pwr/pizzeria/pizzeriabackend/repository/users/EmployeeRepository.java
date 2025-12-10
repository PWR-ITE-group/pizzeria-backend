package pl.edu.pwr.pizzeria.pizzeriabackend.repository.users;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;

import java.util.List;
import java.util.Optional;

/**
 * Custom DAO (Data Access Object) implementation for Employee entity.
 * Uses raw EntityManager to perform CRUD operations.
 */
@Repository
public class EmployeeRepository {

    // Injection of the JPA EntityManager, which acts as the interface to the Persistence Context.
    @PersistenceContext
    private EntityManager entityManager;

    // -----------------------------------------------------------
    // 1. BASIC CRUD (Create, Read, Update, Delete)
    // -----------------------------------------------------------

    /**
     * Saves a given entity.
     * Logic:
     * - If the ID is null, the entity is new, so we use persist() to INSERT it.
     * - If the ID exists, the entity is detached, so we use merge() to UPDATE it.
     *
     * @param employee The entity to save.
     * @return The saved or updated entity.
     */
    public Employee save(Employee employee) {
        if (employee.getId() == null) {
            // Adds the entity to the Persistence Context (will be INSERTed at commit)
            entityManager.persist(employee);
            return employee;
        } else {
            // Merges changes from the provided object into the Persistence Context (UPDATE)
            return entityManager.merge(employee);
        }
    }

    /**
     * Finds an Employee by their primary key (ID).
     *
     * @param id The ID of the employee.
     * @return An Optional containing the employee if found, or empty otherwise.
     */
    public Optional<Employee> findById(Long id) {
        // find() looks up the entity in the L1 Cache first, then the Database.
        Employee employee = entityManager.find(Employee.class, id);
        return Optional.ofNullable(employee);
    }

    /**
     * Retrieves all employees from the database.
     * Uses JPQL (Java Persistence Query Language), which operates on Entities, not tables.
     *
     * @return A list of all employees.
     */
    public List<Employee> findAll() {
        String jpql = "SELECT e FROM Employee e";
        TypedQuery<Employee> query = entityManager.createQuery(jpql, Employee.class);
        return query.getResultList();
    }

    /**
     * Deletes an employee by ID.
     * Note: In JPA, an object must be "managed" (attached to context) before it can be removed.
     *
     * @param id The ID of the employee to delete.
     */
    public void deleteById(Long id) {
        // First, we must fetch the entity to make it managed.
        Employee employee = entityManager.find(Employee.class, id);

        if (employee != null) {
            // Marks the entity for deletion (DELETE SQL will be sent at commit)
            entityManager.remove(employee);
        }
    }

    // -----------------------------------------------------------
    // 2. CUSTOM METHODS (Specific Business Logic)
    // -----------------------------------------------------------

    /**
     * Finds an employee by their login string.
     * Used for authentication processes.
     *
     * @param login The login to search for.
     * @return Optional<Employee>
     */
    public Optional<Employee> findByLogin(String login) {
        try {
            String jpql = "SELECT e FROM Employee e WHERE e.login = :login";
            TypedQuery<Employee> query = entityManager.createQuery(jpql, Employee.class);
            query.setParameter("login", login); // Parameter binding prevents SQL Injection

            // getSingleResult throws an exception if no result is found
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Checks if a login is already taken.
     * Optimized: Uses COUNT instead of fetching the whole entity.
     *
     * @param login The login to check.
     * @return true if exists, false otherwise.
     */
    public boolean existsByLogin(String login) {
        String jpql = "SELECT COUNT(e) FROM Employee e WHERE e.login = :login";
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("login", login)
                .getSingleResult();
        return count > 0;
    }

    /**
     * Checks if a phone number is already registered.
     *
     * @param phone The phone number to check.
     * @return true if exists, false otherwise.
     */
    public boolean existsByPhone(String phone) {
        String jpql = "SELECT COUNT(e) FROM Employee e WHERE e.phone = :phone";
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("phone", phone)
                .getSingleResult();
        return count > 0;
    }

    /**
     * Finds a list of employees filtering by their role (e.g., 'chef', 'waiter').
     *
     * @param role The role to filter by.
     * @return List of matching employees.
     */
    public List<Employee> findByRole(String role) {
        String jpql = "SELECT e FROM Employee e WHERE e.role = :role";
        TypedQuery<Employee> query = entityManager.createQuery(jpql, Employee.class);
        query.setParameter("role", role);
        return query.getResultList();
    }

    public void flush() {
        entityManager.flush();
    }

    public long count() {
        String jpql = "SELECT COUNT(e) FROM Employee e";
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }

    public void deleteAll() {
        entityManager.createQuery("DELETE FROM Employee").executeUpdate();
    }

    public void saveAll(List<Employee> employees) {
        for (Employee emp : employees) {
            save(emp);
        }
    }
}
