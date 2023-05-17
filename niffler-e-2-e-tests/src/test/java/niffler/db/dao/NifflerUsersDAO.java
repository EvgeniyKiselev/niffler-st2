package niffler.db.dao;

import niffler.db.entity.UserEntity;

import java.util.UUID;

public interface NifflerUsersDAO {

  int createUser(UserEntity user);

  UUID getUserId(UserEntity user);

  UserEntity selectUser(String userName);

  int updateUser(String userName, UserEntity user);

  int deleteUser(UserEntity user);

  UserEntity readUser(UUID uuid);

}