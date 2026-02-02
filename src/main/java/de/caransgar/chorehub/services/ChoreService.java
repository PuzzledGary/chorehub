package de.caransgar.chorehub.services;

import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.entity.User;
import de.caransgar.chorehub.repository.ChoreRepository;
import de.caransgar.chorehub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChoreService {

    @Autowired
    private ChoreRepository choreRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Chore> getAllChores() {
        return choreRepository.findAll();
    }

    public Optional<Chore> getChoreById(Long id) {
        return choreRepository.findById(id);
    }

    public Chore saveChore(Chore chore) {
        return choreRepository.save(chore);
    }

    public void deleteChore(Long id) {
        choreRepository.deleteById(id);
    }

    public List<Chore> getChoresByUser(User user) {
        return choreRepository.findByAssignedUser(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

}