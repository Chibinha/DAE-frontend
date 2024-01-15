package com.example.backend.ejbs;

import com.example.backend.entities.Client;
import com.example.backend.exceptions.MyConstraintViolationException;
import com.example.backend.exceptions.MyEntityExistsException;
import com.example.backend.exceptions.MyEntityNotFoundException;
import com.example.backend.security.Hasher;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.Hibernate;

import java.util.List;

@Stateless
public class ClientBean {

    @PersistenceContext
    private EntityManager entityManager;
    private Hasher hasher;

    public Client find(long id) throws MyEntityNotFoundException {
        Client client = entityManager.find(Client.class, id);
        if (client == null) {
            throw new MyEntityNotFoundException("Client with id " + id + " not found");
        }
        return client;
    }

    public Client find(String username) {
        return entityManager.find(Client.class, username);

    }

    public void create(String username, String password, String name, String email) throws MyEntityExistsException, MyConstraintViolationException {

        if(this.find(username)== null)
        {
            try {
                hasher = new Hasher();
                Client client = new Client(username, hasher.hash(password), name, email);
                entityManager.persist(client);
                entityManager.flush(); // when using Hibernate, to force it to throw a ContraintViolationException, as in the JPA specification
                entityManager.persist(client);
            }
            catch (ConstraintViolationException e) {
                throw new MyConstraintViolationException(e);
            }
        }
        else {
            throw new MyEntityExistsException(" ERROR -  The username [" + username + "] is already in use!!!");
        }
    }

    public void update(String username, String password, String name, String email,long courseCode) {
        Client client = entityManager.find(Client.class, username);
        if (client == null) {
            System.err.println("ERROR_CLIENT_NOT_FOUND: " + username);
            return;
        }
        entityManager.lock(client, LockModeType.OPTIMISTIC);
        client.setPassword(password);
        client.setName(name);
        client.setEmail(email);
    }

    public void delete(long id) throws MyEntityNotFoundException{
        entityManager.remove(find(id));
    }

    public List<Client> getAllClients() {
        return entityManager.createNamedQuery("getAllClients", Client.class).getResultList();
    }



    public Client getClientOrders(String username) {
        Client client = this.find(username);
        if(client != null)
        {
            Hibernate.initialize(client.getOrders());
            return this.find(username);
        }
        return null;
    }
}