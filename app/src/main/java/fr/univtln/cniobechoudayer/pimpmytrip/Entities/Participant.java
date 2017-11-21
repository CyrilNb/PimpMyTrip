package fr.univtln.cniobechoudayer.pimpmytrip.Entities;

/**
 * Participant class which extends User
 * Created by Cyril Niobé on 22/11/2017.
 */

public class Participant extends User {

    /**
     * Constructor of a participant
     *
     * @param idUser id of the participant
     * @param pseudo pseudo of the participant
     */
    public Participant(int idUser, String pseudo) {
        super(idUser, pseudo);
    }
}
