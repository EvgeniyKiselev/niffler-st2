package niffler.db.dao;

import niffler.db.entity.UserEntity;

import java.util.UUID;

public interface NifflerUsersDAO {

  int createUser(UserEntity user);

  String getUserId(String userName);

  UserEntity selectUser(UUID userId);

  int updateUser(String userName, UserEntity user);

  int deleteUser(String userName);

}