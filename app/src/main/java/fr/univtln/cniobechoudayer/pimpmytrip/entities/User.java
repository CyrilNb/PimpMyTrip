package fr.univtln.cniobechoudayer.pimpmytrip.entities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * User abtrsact class
 * Created by Cyril Niobé on 22/11/2017.
 */

public class User {

    /***********
     * MEMBERS *
     ***********/

    private int idUser;
    private String pseudo;
    private String email;
    private String photo;

    /**
     * Constructor of a user
     * @param email email of the user
     * @param pseudo pseudo of the user
     */
    public User(String pseudo,String email) {
        this.pseudo = pseudo;
        this.email = email;
    }

    /**
     * Constructor of a user
     * @param email email of the user
     */
    public User(String email){
        this.email = email;
    }

    /**
     * Default constructor required for calls to
     * DataSnapshot.getValue(User.class)
     */
    public User() {
    }

    /*********************
     * GETTER AND SETTER *
     *********************/

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
     * Get the email of the user
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the email of the user
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
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
    public String getPhoto() {
        return photo;
    }

    /**
     * Set the photo of the user
     * @param photo
     */
    private void setPhoto(String photo) {
        this.photo = photo;
    }

    public Bitmap getConvertedPhoto(){
        byte[] decodedString = Base64.decode(this.photo, Base64.DEFAULT);
        Bitmap decodedBytePhoto = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedBytePhoto;
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
                ", email='" + email + '\'' +
                ", photo='" + photo + '\'' +
                '}';
    }
}
