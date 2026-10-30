package com.juniverse.quarkus.crud.service;

import com.juniverse.quarkus.crud.dto.CreateUserDTO;
import com.juniverse.quarkus.crud.dto.UserDTO;
import com.juniverse.quarkus.crud.entity.User;
import com.juniverse.quarkus.crud.exception.BusinessException;
import com.juniverse.quarkus.crud.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Lógica de negocio de usuarios. */
@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    /** Todos los usuarios. */
    public List<UserDTO> getAllUsers() {
        return userRepository.listAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca por id.
     * @param id identificador
     */
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findByIdOptional(id)
                .map(this::convertToDTO);
    }

    /**
     * Crea un usuario.
     * @param createUserDTO datos
     * @throws BusinessException si el correo ya existe
     */
    @Transactional
    public UserDTO createUser(CreateUserDTO createUserDTO) {
        if (userRepository.existsByEmail(createUserDTO.email)) {
            throw new BusinessException("Ya existe un usuario con el email: " + createUserDTO.email);
        }

        User user = new User(createUserDTO.name, createUserDTO.email, createUserDTO.phone, createUserDTO.city);
        userRepository.persist(user);
        
        return convertToDTO(user);
    }

    /**
     * Actualiza un usuario.
     * @param id identificador
     * @param updateUserDTO nuevos datos
     * @return el usuario, o vacío si no existe
     * @throws BusinessException si otro usuario ya usa el correo
     */
    @Transactional
    public Optional<UserDTO> updateUser(Long id, CreateUserDTO updateUserDTO) {
        Optional<User> optionalUser = userRepository.findByIdOptional(id);
        
        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        User user = optionalUser.get();

        if (userRepository.existsByEmailAndIdNot(updateUserDTO.email, id)) {
            throw new BusinessException("Ya existe otro usuario con el email: " + updateUserDTO.email);
        }

        user.name = updateUserDTO.name;
        user.email = updateUserDTO.email;
        user.phone = updateUserDTO.phone;
        user.city = updateUserDTO.city;

        userRepository.persist(user);
        
        return Optional.of(convertToDTO(user));
    }

    /**
     * Elimina un usuario.
     * @param id identificador
     * @return {@code true} si existía
     */
    @Transactional
    public boolean deleteUser(Long id) {
        return userRepository.deleteById(id);
    }

    /**
     * Busca por correo.
     * @param email correo
     */
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    /** Convierte la entidad en DTO. */
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.id,
                user.name,
                user.email,
                user.phone,
                user.city,
                user.createdAt,
                user.updatedAt
        );
    }
}