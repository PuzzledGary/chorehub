package de.caransgar.chorehub.services;

import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.repository.ChoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChoreService {

    @Autowired
    private ChoreRepository choreRepository;

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

    public List<Chore> getChoresByUserId(Long userId) {
        return choreRepository.findByAssignedUserId(userId);
    }

}