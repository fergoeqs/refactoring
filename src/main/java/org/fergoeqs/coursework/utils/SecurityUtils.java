package org.fergoeqs.coursework.utils;

import org.fergoeqs.coursework.exception.ForbiddenException;
import org.fergoeqs.coursework.models.AppUser;
import org.fergoeqs.coursework.models.Appointment;
import org.fergoeqs.coursework.models.Pet;
import org.fergoeqs.coursework.models.enums.RoleType;


public class SecurityUtils {

    public static void checkPetAccess(AppUser currentUser, Pet pet, boolean requireWrite) {
        boolean hasAccess = false;

        if (currentUser.getRoles().contains(RoleType.ROLE_ADMIN)) {
            hasAccess = true;
        }
        else if (currentUser.getRoles().contains(RoleType.ROLE_OWNER) && 
                 pet.getOwner() != null && pet.getOwner().getId().equals(currentUser.getId())) {
            hasAccess = true;
        }
        else if (currentUser.getRoles().contains(RoleType.ROLE_VET) && 
                 pet.getActualVet() != null && pet.getActualVet().getId().equals(currentUser.getId())) {
            hasAccess = true;
        }

        if (!hasAccess) {
            throw new ForbiddenException("Access denied to pet with id: " + pet.getId());
        }
    }


    public static void checkUserAccess(AppUser currentUser, Long targetUserId, boolean requireWrite) {
        boolean hasAccess = false;

        if (currentUser.getRoles().contains(RoleType.ROLE_ADMIN)) {
            hasAccess = true;
        }
        else if (currentUser.getId().equals(targetUserId)) {
            hasAccess = true;
        }

        if (!hasAccess) {
            throw new ForbiddenException("Access denied to user with id: " + targetUserId);
        }
    }


    public static void checkAppointmentAccess(AppUser currentUser, Appointment appointment, boolean requireWrite) {
        boolean hasAccess = false;

        if (currentUser.getRoles().contains(RoleType.ROLE_ADMIN)) {
            hasAccess = true;
        }
        else if (currentUser.getRoles().contains(RoleType.ROLE_OWNER) && 
                 appointment.getPet() != null && 
                 appointment.getPet().getOwner() != null && 
                 appointment.getPet().getOwner().getId().equals(currentUser.getId())) {
            hasAccess = true;
        }
        else if (currentUser.getRoles().contains(RoleType.ROLE_VET) && 
                 appointment.getSlot() != null && 
                 appointment.getSlot().getVet() != null && 
                 appointment.getSlot().getVet().getId().equals(currentUser.getId())) {
            hasAccess = true;
        }

        if (!hasAccess) {
            throw new ForbiddenException("Access denied to appointment with id: " + appointment.getId());
        }
    }


    public static boolean isAdmin(AppUser user) {
        return user.getRoles().contains(RoleType.ROLE_ADMIN);
    }


    public static boolean isVet(AppUser user) {
        return user.getRoles().contains(RoleType.ROLE_VET);
    }

    public static boolean isOwner(AppUser user) {
        return user.getRoles().contains(RoleType.ROLE_OWNER);
    }


    public static void checkResourceAccessThroughPet(AppUser currentUser, Pet pet, boolean requireWrite) {
        checkPetAccess(currentUser, pet, requireWrite);
    }
}
