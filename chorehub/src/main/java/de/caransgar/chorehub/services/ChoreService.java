package de.caransgar.chorehub.services;

import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.entity.User;
import de.caransgar.chorehub.repository.ChoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChoreService {

    private final ChoreRepository choreRepository;

    public ChoreService(ChoreRepository choreRepository, UserService userService) {
        this.choreRepository = choreRepository;
    }

    public List<Chore> getAllChores() {
        return choreRepository.findAll();
    }

    public List<Chore> getUsersChores(Long userId) {
        return choreRepository.findByUserId(userId);
    }

    public List<Chore> getUsersChores(User user) {
        return choreRepository.findByAssignedUser(user);
    }

    public Optional<Chore> getChoreById(Long id) {
        return choreRepository.findById(id);
    }

    public Optional<Chore> markChoreAsDone(Long id) {
        Optional<Chore> choreOptional = getChoreById(id);

        if (choreOptional.isPresent()) {
            markChoreAsDone(choreOptional.get());
        }

        return getChoreById(id);
    }

    public Chore markChoreAsDone(Chore chore) {
        // TODO: Write History

        chore.completeChore();
        return saveChore(chore);
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

}