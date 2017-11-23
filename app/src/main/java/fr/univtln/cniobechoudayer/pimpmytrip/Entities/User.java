package fr.univtln.cniobechoudayer.pimpmytrip.Entities;

import android.graphics.Bitmap;

/**
 * User abtrsact class
 * Created by Cyril Niobé on 22/11/2017.
 */

//abstract ? donc un seul host ? un utilisateur peut pas devenir organisateur a tout moment ?
public class User {

    /**
     * MEMBERS
     */
    private int idUser;
    private String pseudo;
    private Bitmap photo;

    /**
     * GETTER AND SETTER
     */

    /**
     * Get the id of the user
     * @return idUser
     */
    public int getIdUser() {
        return idUser;
    }

    /**
     * Set the id of the user
     * @param idUser id to be set
     */
    private void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    /**
     * Get the pseudo of the user
     * @return pseudo
     */
    public String getPseudo() {
        return pseudo;
    }

    /**
     * Set the pseudo of the user
     * @param pseudo to be set
     */
    private void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    /**
     * Get the photo of the user
     * @return photo of the user
     */
    public Bitmap getPhoto() {
        return photo;
    }

    /**
     * Set the photo of the user
     * @param photo
     */
    private void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    //TODO BUILDER pattern
    /**
     * Constructor of a user
     * @param idUser
     * @param pseudo
     */
    public User(int idUser, String pseudo) {
        this.idUser = idUser;
        this.pseudo = pseudo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (getIdUser() != user.getIdUser()) return false;
        return getPseudo() != null ? getPseudo().equals(user.getPseudo()) : user.getPseudo() == null;

    }

    @Override
    public int hashCode() {
        int result = getIdUser();
        result = 31 * result + (getPseudo() != null ? getPseudo().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "idUser=" + idUser +
                ", pseudo='" + pseudo + '\'' +
                '}';
    }
}
