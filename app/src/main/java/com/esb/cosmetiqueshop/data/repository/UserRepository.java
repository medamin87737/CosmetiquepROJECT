package com.esb.cosmetiqueshop.data.repository;

import android.content.Context;

import com.esb.cosmetiqueshop.data.dao.UserDao;
import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.model.User;
import com.esb.cosmetiqueshop.utils.AppExecutors;
import com.esb.cosmetiqueshop.utils.SessionManager;
import com.esb.cosmetiqueshop.utils.Validators;

public class UserRepository {

    public interface AuthCallback {
        void onSuccess(User user);

        void onFailure(String message);
    }

    public interface SimpleCallback<T> {
        void onResult(T result);
    }

    private final Context appContext;
    private final UserDao userDao;
    private final SessionManager sessionManager;
    private final AppExecutors executors;

    public UserRepository(Context context) {
        this.appContext = context.getApplicationContext();
        DatabaseHelper helper = new DatabaseHelper(context);
        this.userDao = new UserDao(helper);
        this.sessionManager = new SessionManager(context);
        this.executors = AppExecutors.getInstance();
    }

    public void login(String email, String password, AuthCallback callback) {
        executors.diskIO().execute(() -> {
            if (!Validators.isValidEmail(email) || !Validators.isValidPassword(password)) {
                executors.mainThread().execute(() ->
                        callback.onFailure(appContext.getString(
                                com.esb.cosmetiqueshop.R.string.error_invalid_credentials)));
                return;
            }
            User pre = userDao.findByEmail(email.trim());
            if (pre != null && !pre.isActive()) {
                executors.mainThread().execute(() -> callback.onFailure("Compte désactivé."));
                return;
            }
            User[] holder = new User[1];
            boolean ok = userDao.verifyCredentials(email.trim(), password, holder);
            executors.mainThread().execute(() -> {
                if (ok && holder[0] != null) {
                    sessionManager.saveSession(holder[0].getId(), holder[0].getRole(), holder[0].getEmail());
                    callback.onSuccess(holder[0]);
                } else {
                    callback.onFailure("Email ou mot de passe incorrect.");
                }
            });
        });
    }

    public void register(String email, String password, String fullName, String phone, AuthCallback callback) {
        executors.diskIO().execute(() -> {
            if (!Validators.isValidEmail(email) || !Validators.isValidPassword(password)
                    || !Validators.isValidTnPhone(phone)) {
                executors.mainThread().execute(() -> callback.onFailure("Vérifiez email, mot de passe et téléphone."));
                return;
            }
            if (userDao.findByEmail(email.trim()) != null) {
                executors.mainThread().execute(() -> callback.onFailure("Cet email est déjà utilisé."));
                return;
            }
            long id = userDao.insertClient(email.trim(), password, fullName.trim(), phone.trim());
            User user = userDao.findById(id);
            executors.mainThread().execute(() -> {
                if (user != null) {
                    sessionManager.saveSession(user.getId(), user.getRole(), user.getEmail());
                    callback.onSuccess(user);
                } else {
                    callback.onFailure("Inscription impossible.");
                }
            });
        });
    }

    public void changePassword(long userId, String newPassword, boolean adminForced, AuthCallback callback) {
        executors.diskIO().execute(() -> {
            if (!Validators.isValidPassword(newPassword)) {
                executors.mainThread().execute(() -> callback.onFailure("Mot de passe trop faible."));
                return;
            }
            boolean ok = userDao.updatePassword(userId, newPassword, adminForced);
            User user = ok ? userDao.findById(userId) : null;
            executors.mainThread().execute(() -> {
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onFailure("Mise à jour impossible.");
                }
            });
        });
    }

    public void setClientActive(long userId, boolean active, AuthCallback callback) {
        executors.diskIO().execute(() -> {
            User user = userDao.findById(userId);
            if (user == null || user.getRole() != com.esb.cosmetiqueshop.model.UserRole.CLIENT) {
                executors.mainThread().execute(() -> callback.onFailure("Client introuvable."));
                return;
            }
            boolean ok = userDao.setActive(userId, active);
            User updated = ok ? userDao.findById(userId) : null;
            executors.mainThread().execute(() -> {
                if (updated != null) {
                    callback.onSuccess(updated);
                } else {
                    callback.onFailure("Mise à jour impossible.");
                }
            });
        });
    }

    public void logout() {
        sessionManager.clearSession();
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void updateProfile(long userId, String fullName, String phone, AuthCallback callback) {
        executors.diskIO().execute(() -> {
            if (!Validators.isValidTnPhone(phone)) {
                executors.mainThread().execute(() -> callback.onFailure("Téléphone invalide."));
                return;
            }
            boolean ok = userDao.updateProfile(userId, fullName.trim(), phone.trim());
            User user = ok ? userDao.findById(userId) : null;
            executors.mainThread().execute(() -> {
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onFailure("Mise à jour impossible.");
                }
            });
        });
    }

    public void listClients(SimpleCallback<java.util.List<User>> callback) {
        executors.diskIO().execute(() -> {
            java.util.List<User> list = userDao.findByRole(com.esb.cosmetiqueshop.model.UserRole.CLIENT);
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void listAllUsers(SimpleCallback<java.util.List<User>> callback) {
        executors.diskIO().execute(() -> {
            java.util.List<User> list = userDao.findAllUsers();
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void getUserById(long userId, SimpleCallback<User> callback) {
        executors.diskIO().execute(() -> {
            User user = userDao.findById(userId);
            executors.mainThread().execute(() -> callback.onResult(user));
        });
    }

    public void adminCreateUser(String email, String password, String fullName, String phone,
                                com.esb.cosmetiqueshop.model.UserRole role, boolean active,
                                AuthCallback callback) {
        executors.diskIO().execute(() -> {
            if (!Validators.isValidEmail(email) || !Validators.isValidPassword(password)
                    || !Validators.isValidTnPhone(phone)) {
                executors.mainThread().execute(() ->
                        callback.onFailure("Vérifiez email, mot de passe et téléphone."));
                return;
            }
            if (userDao.findByEmail(email.trim()) != null) {
                executors.mainThread().execute(() -> callback.onFailure("Cet email est déjà utilisé."));
                return;
            }
            com.esb.cosmetiqueshop.model.UserRole safeRole =
                    role == null ? com.esb.cosmetiqueshop.model.UserRole.CLIENT : role;
            long id = userDao.insertUser(email.trim(), password, fullName.trim(), phone.trim(),
                    safeRole, safeRole == com.esb.cosmetiqueshop.model.UserRole.ADMIN, active);
            User user = userDao.findById(id);
            executors.mainThread().execute(() -> {
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onFailure("Création impossible.");
                }
            });
        });
    }

    public void adminUpdateUser(long userId, String email, String fullName, String phone,
                                com.esb.cosmetiqueshop.model.UserRole role, boolean active,
                                String newPasswordOrNull, AuthCallback callback) {
        executors.diskIO().execute(() -> {
            User existing = userDao.findById(userId);
            if (existing == null) {
                executors.mainThread().execute(() -> callback.onFailure("Utilisateur introuvable."));
                return;
            }
            if (!Validators.isValidEmail(email) || !Validators.isValidTnPhone(phone)) {
                executors.mainThread().execute(() -> callback.onFailure("Email ou téléphone invalide."));
                return;
            }
            User other = userDao.findByEmail(email.trim());
            if (other != null && other.getId() != userId) {
                executors.mainThread().execute(() -> callback.onFailure("Cet email est déjà utilisé."));
                return;
            }
            if (newPasswordOrNull != null && !newPasswordOrNull.isEmpty()
                    && !Validators.isValidPassword(newPasswordOrNull)) {
                executors.mainThread().execute(() -> callback.onFailure("Mot de passe trop faible."));
                return;
            }
            com.esb.cosmetiqueshop.model.UserRole safeRole =
                    role == null ? existing.getRole() : role;
            boolean ok = userDao.updateUserAdmin(userId, email.trim(), fullName.trim(), phone.trim(),
                    safeRole, active);
            if (ok && newPasswordOrNull != null && !newPasswordOrNull.isEmpty()) {
                ok = userDao.updatePassword(userId, newPasswordOrNull, true);
            }
            User updated = ok ? userDao.findById(userId) : null;
            executors.mainThread().execute(() -> {
                if (updated != null) {
                    callback.onSuccess(updated);
                } else {
                    callback.onFailure("Mise à jour impossible.");
                }
            });
        });
    }

    public void setUserActive(long userId, boolean active, AuthCallback callback) {
        executors.diskIO().execute(() -> {
            User user = userDao.findById(userId);
            if (user == null) {
                executors.mainThread().execute(() -> callback.onFailure("Utilisateur introuvable."));
                return;
            }
            if (user.getId() == sessionManager.getUserId()) {
                executors.mainThread().execute(() ->
                        callback.onFailure("Impossible de désactiver votre propre compte."));
                return;
            }
            boolean ok = userDao.setActive(userId, active);
            User updated = ok ? userDao.findById(userId) : null;
            executors.mainThread().execute(() -> {
                if (updated != null) {
                    callback.onSuccess(updated);
                } else {
                    callback.onFailure("Mise à jour impossible.");
                }
            });
        });
    }

    public void getCurrentUser(SimpleCallback<User> callback) {
        long id = sessionManager.getUserId();
        if (id <= 0) {
            executors.mainThread().execute(() -> callback.onResult(null));
            return;
        }
        executors.diskIO().execute(() -> {
            User user = userDao.findById(id);
            executors.mainThread().execute(() -> callback.onResult(user));
        });
    }
}