package eu.arrowhead.core.authorization.database.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.AuthorizationInterCloud;
import eu.arrowhead.common.database.entity.AuthorizationInterCloudInterfaceConnection;
import eu.arrowhead.common.database.entity.AuthorizationIntraCloud;
import eu.arrowhead.common.database.entity.AuthorizationIntraCloudInterfaceConnection;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.AuthorizationInterCloudInterfaceConnectionRepository;
import eu.arrowhead.common.database.repository.AuthorizationInterCloudRepository;
import eu.arrowhead.common.database.repository.AuthorizationIntraCloudInterfaceConnectionRepository;
import eu.arrowhead.common.database.repository.AuthorizationIntraCloudRepository;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceInterfaceRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.AuthorizationInterCloudCheckResponseDTO;
import eu.arrowhead.common.dto.AuthorizationInterCloudListResponseDTO;
import eu.arrowhead.common.dto.AuthorizationInterCloudResponseDTO;
import eu.arrowhead.common.dto.AuthorizationIntraCloudCheckResponseDTO;
import eu.arrowhead.common.dto.AuthorizationIntraCloudListResponseDTO;
import eu.arrowhead.common.dto.AuthorizationIntraCloudResponseDTO;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.IdIdListDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@Service
public class AuthorizationDBService {
	
	//=================================================================================================
	// members
	
	@Autowired
	private AuthorizationIntraCloudRepository authorizationIntraCloudRepository;
	
	@Autowired
	private AuthorizationInterCloudRepository authorizationInterCloudRepository;
	
	@Autowired
	private CloudRepository cloudRepository;
	
	@Autowired
	private SystemRepository systemRepository;
	
	@Autowired
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	@Autowired
	private ServiceInterfaceRepository serviceInterfaceRepository;
	
	@Autowired
	private AuthorizationIntraCloudInterfaceConnectionRepository authorizationIntraCloudInterfaceConnectionRepository;
	
	@Autowired
	private AuthorizationInterCloudInterfaceConnectionRepository authorizationInterCloudInterfaceConnectionRepository;
	
	private final Logger logger = LogManager.getLogger(AuthorizationDBService.class);

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Page<AuthorizationIntraCloud> getAuthorizationIntraCloudEntries(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getAuthorizationIntraCloudEntries started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size; 		
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!AuthorizationIntraCloud.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		try {
			return authorizationIntraCloudRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudListResponseDTO getAuthorizationIntraCloudEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getAuthorizationIntraCloudEntriesResponse started...");
		final Page<AuthorizationIntraCloud> authorizationIntraCloudEntries = getAuthorizationIntraCloudEntries(page, size, direction, sortField);
		
		return DTOConverter.convertAuthorizationIntraCloudListToAuthorizationIntraCloudListResponseDTO(authorizationIntraCloudEntries);
	}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloud getAuthorizationIntraCloudEntryById(final long id) {
		logger.debug("getAuthorizationIntraCloudEntryById started...");
		
		try {
			final Optional<AuthorizationIntraCloud> find = authorizationIntraCloudRepository.findById(id);
			if (find.isPresent()) {
				return find.get();
			} else {
				throw new InvalidParameterException("AuthorizationIntraCloud with id of '" + id + "' not exists");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudResponseDTO getAuthorizationIntraCloudEntryByIdResponse(final long id) {
		logger.debug("getAuthorizationIntraCloudEntryByIdResponse started...");		
		final AuthorizationIntraCloud authorizationIntraCloudEntry = getAuthorizationIntraCloudEntryById(id);
		
		return DTOConverter.convertAuthorizationIntraCloudToAuthorizationIntraCloudResponseDTO(authorizationIntraCloudEntry);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeAuthorizationIntraCloudEntryById(final long id) {
		logger.debug("removeAuthorizationIntraCloudEntryById started...");
		
		try {
			if (!authorizationIntraCloudRepository.existsById(id)) {
				throw new InvalidParameterException("AuthorizationIntraCloud with id of '" + id + "' not exists");
			}
			
			authorizationIntraCloudRepository.deleteById(id);
			authorizationIntraCloudRepository.flush();
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	@Transactional(rollbackFor = ArrowheadException.class)
	public AuthorizationIntraCloud createAuthorizationIntraCloud(final long consumerId, final long providerId, final long serviceDefinitionId) {
		logger.debug("createAuthorizationIntraCloud started...");
		
		final boolean consumerIdIsInvalid = consumerId < 1;
		final boolean providerIdIsInvalid = providerId < 1;
		final boolean serviceDefinitionIdIsInvalid = serviceDefinitionId < 1;
		try {
			if (consumerIdIsInvalid || providerIdIsInvalid || serviceDefinitionIdIsInvalid) {
				String exceptionMessage = "Following id parameters are invalid:";
				exceptionMessage = consumerIdIsInvalid ? exceptionMessage : exceptionMessage + " consumerId," ;
				exceptionMessage = providerIdIsInvalid ? exceptionMessage : exceptionMessage + " providerId,";
				exceptionMessage = serviceDefinitionIdIsInvalid ? exceptionMessage : exceptionMessage + " serviceDefinitionId,";
				exceptionMessage = exceptionMessage.substring(0, exceptionMessage.length() - 1);
				
				throw new InvalidParameterException(exceptionMessage);
			}	
			
			final Optional<System> consumer = systemRepository.findById(consumerId);
			final Optional<System> provider = systemRepository.findById(providerId);
			final Optional<ServiceDefinition> serviceDefinition = serviceDefinitionRepository.findById(serviceDefinitionId);
			if (consumer.isEmpty() || provider.isEmpty() || serviceDefinition.isEmpty()) {
				String exceptionMessage = "Following entities are not available:";
				exceptionMessage = consumer.isEmpty() ? exceptionMessage + " consumer with id: " + consumerId + "," : exceptionMessage;
				exceptionMessage = provider.isEmpty() ? exceptionMessage + " provider with id: " + providerId + "," : exceptionMessage;
				exceptionMessage = serviceDefinition.isEmpty() ? exceptionMessage + " serviceDefinition with id: " + serviceDefinitionId + "," : exceptionMessage;
				exceptionMessage = exceptionMessage.substring(0, exceptionMessage.length() - 1);
				
				throw new InvalidParameterException(exceptionMessage);
			}
			
			checkConstraintsOfAuthorizationIntraCloudTable(consumer.get(), provider.get(), serviceDefinition.get());
			final AuthorizationIntraCloud authorizationIntraCloud = new AuthorizationIntraCloud(consumer.get(), provider.get(), serviceDefinition.get());
			
			return authorizationIntraCloudRepository.saveAndFlush(authorizationIntraCloud);
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public AuthorizationIntraCloudResponseDTO createAuthorizationIntraCloudResponse(final long consumerId, final long providerId, final long serviceDefinitionId) {
		logger.debug("createAuthorizationIntraCloudResponse started...");
		final AuthorizationIntraCloud entry = createAuthorizationIntraCloud(consumerId, providerId, serviceDefinitionId);
		
		return DTOConverter.convertAuthorizationIntraCloudToAuthorizationIntraCloudResponseDTO(entry);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public List<AuthorizationIntraCloud> createBulkAuthorizationIntraCloud(final long consumerId, final Set<Long> providerIds, final Set<Long> serviceDefinitionIds, final Set<Long> interfaceIds) {
		logger.debug("createBulkAuthorizationIntraCloud started...");
		
		if (consumerId < 1) {
			throw new InvalidParameterException("Consumer id can't be null and must be greater than 0.");
		}		
		if (providerIds == null || providerIds.isEmpty()) {
			throw new InvalidParameterException("providerIds list is empty");
		}		
		if (serviceDefinitionIds == null || serviceDefinitionIds.isEmpty()) {
			throw new InvalidParameterException("serviceDefinitionIds list is empty");
		}		
		if (interfaceIds == null || interfaceIds.isEmpty()) {
			throw new InvalidParameterException("interfaceIds list is empty");
		}		
		if (providerIds.size() > 1 && serviceDefinitionIds.size() > 1) {
			throw new InvalidParameterException("providerIds list or serviceDefinitionIds list should contain only one element, but both contain more");
		}		
		if (serviceDefinitionIds.size() > 1 && interfaceIds.size() > 1) {
			throw new InvalidParameterException("serviceDefinitionIds list or interfaceIds list should contain only one element, but both contain more");
		}		
		for (final Long id : providerIds) {
			if (id == null || id < 1) {
				throw new InvalidParameterException("Provider id can't be null and must be greater than 0.");
			}
		}		
		for (final Long id : serviceDefinitionIds) {
			if (id == null || id < 1) {
				throw new InvalidParameterException("ServiceDefinition id can't be null and must be greater than 0.");
			}
		}		
		for (final Long id : interfaceIds) {
			if (id == null || id < 1) {
				throw new InvalidParameterException("ServiceInterface id can't be null and must be greater than 0.");
			}
		}
		
		try {
			
			final Optional<System> consumerOpt = systemRepository.findById(consumerId);
			System consumer;
			if (consumerOpt.isPresent()) {
				consumer = consumerOpt.get();
			} else {
				throw new InvalidParameterException("Consumer system with id of " + consumerId + " not exists");
			}
			
			if (providerIds.size() <= serviceDefinitionIds.size() && serviceDefinitionIds.size() >= interfaceIds.size()) {
				// Case: One provider with more or one service and with one interface
				final Long providerId = providerIds.iterator().next();
				final Long interfaceId = interfaceIds.iterator().next();
				return createBulkAuthorizationIntraCloudWithOneProviderAndMoreServiceDefinitionAndOneInterface(consumer, providerId, serviceDefinitionIds, interfaceId);
			} else {
				// Case: One service with more or one provider and with more or one interface
				final Long serviceId = serviceDefinitionIds.iterator().next();
				return createBulkAuthorizationIntraCloudWithOneServiceDefinitionAndMoreProviderAndMoreInterface(consumer, providerIds, serviceId, interfaceIds);
			}
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudListResponseDTO getAuthorizationInterCloudEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getAuthorizationInterCloudEntriesResponse started...");
		final Page<AuthorizationInterCloud> authorizationInterCloudEntries = getAuthorizationInterCloudEntries(page, size, direction, sortField);
		
		return DTOConverter.convertAuthorizationInterCloudListToAuthorizationInterCloudListResponseDTO(authorizationInterCloudEntries);
	}
	
	//-------------------------------------------------------------------------------------------------
	public Page<AuthorizationInterCloud> getAuthorizationInterCloudEntries(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getAuthorizationInterCloudEntries started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size; 		
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!AuthorizationInterCloud.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		try {
			return authorizationInterCloudRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudResponseDTO getAuthorizationInterCloudEntryByIdResponse(final long id) {
		logger.debug("getAuthorizationInterCloudEntryByIdResponse started...");		
		final AuthorizationInterCloud authorizationInterCloudEntry = getAuthorizationInterCloudEntryById(id);
		
		return DTOConverter.convertAuthorizationInterCloudToAuthorizationInterCloudResponseDTO(authorizationInterCloudEntry);
	}
	
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloud getAuthorizationInterCloudEntryById(final long id) {
		logger.debug("getAuthorizationInterCloudEntryById started...");
		
		try {
			final Optional<AuthorizationInterCloud> find = authorizationInterCloudRepository.findById(id);
			if (find.isPresent()) {
				return find.get();
			} else {
				throw new InvalidParameterException("AuthorizationInterCloud with id of '" + id + "' not exists");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public AuthorizationInterCloudListResponseDTO createBulkAuthorizationInterCloudResponse(final long cloudId, final Set<Long> providerIdSet, final Set<Long> serviceDefinitionIdSet, final Set<Long> interfaceIdSet) {
		logger.debug("createBulkAuthorizationInterCloudResponse started...");
		
		try {						
			final List<AuthorizationInterCloud> savedEntries = createBulkAuthorizationInterCloud(cloudId, providerIdSet, serviceDefinitionIdSet, interfaceIdSet);
			final Page<AuthorizationInterCloud> savedEntriesPage = new PageImpl<AuthorizationInterCloud>(savedEntries);
			
			return DTOConverter.convertAuthorizationInterCloudListToAuthorizationInterCloudListResponseDTO(savedEntriesPage);
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)	
	public List<AuthorizationInterCloud> createBulkAuthorizationInterCloud(final long cloudId, final Set<Long> providerIdSet, final Set<Long> serviceDefinitionIdSet, final Set<Long> interfaceIdSet) {
		logger.debug("createBulkAuthorizationInterCloud started...");
		
		if (cloudId < 1) {
			throw new InvalidParameterException("cloud id can't be null and must be greater than 0.");
		}		
		if (providerIdSet == null || providerIdSet.isEmpty()) {
			throw new InvalidParameterException("providerId list is empty");
		}		
		if (serviceDefinitionIdSet == null || serviceDefinitionIdSet.isEmpty()) {
			throw new InvalidParameterException("serviceDefinitionId list is empty");
		}		
		if (interfaceIdSet == null || interfaceIdSet.isEmpty()) {
			throw new InvalidParameterException("interfaceId list is empty");
		}
		if (providerIdSet.size() > 1 && serviceDefinitionIdSet.size() > 1) {
			throw new InvalidParameterException("providerId list or serviceDefinitionId list should contain only one element, but both contain more");
		}
		if (serviceDefinitionIdSet.size() > 1 && interfaceIdSet.size() > 1) {
			throw new InvalidParameterException("serviceDefinitionId list or interfaceId list should contain only one element, but both contain more");
		}
		for (final Long id : providerIdSet) {
			if (id == null || id < 1) {
				throw new InvalidParameterException("Provider id can't be null and must be greater than 0.");
			}
		}
		for (final Long id : serviceDefinitionIdSet) {
			if (id == null || id < 1) {
				throw new InvalidParameterException("ServiceDefinition id can't be null and must be greater than 0.");
			}
		}
		for (final Long id : interfaceIdSet) {
			if (id == null || id < 1) {
				throw new InvalidParameterException("ServiceInterface id can't be null and must be greater than 0.");
			}
		}
				
		try {
			
			final Optional<Cloud> cloudOpt = cloudRepository.findById(cloudId);
			Cloud cloud;
			if (cloudOpt.isPresent()) {
				cloud = cloudOpt.get();
			} else {
				throw new InvalidParameterException("Cloud with id of " + cloudId + " not exists");
			}
			
			if (providerIdSet.size() <= serviceDefinitionIdSet.size() && serviceDefinitionIdSet.size() >= interfaceIdSet.size()) {
				// Case: One provider with more or one service and with one interface
				final Long providerId = providerIdSet.iterator().next();
				final Long interfaceId = interfaceIdSet.iterator().next();
				return createBulkAuthorizationInterCloudWithOneProviderAndMoreServiceDefinitionAndOneInterface(cloud, providerId, serviceDefinitionIdSet, interfaceId);
			} else {
				// Case: One service with more or one provider and with more or one interface
				final Long serviceId = serviceDefinitionIdSet.iterator().next();
				return createBulkAuthorizationInterCloudWithOneServiceDefinitionAndMoreProviderAndMoreInterface(cloud, providerIdSet, serviceId, interfaceIdSet);
			}
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public AuthorizationIntraCloudListResponseDTO createBulkAuthorizationIntraCloudResponse(final long consumerId, final Set<Long> providerIds, final Set<Long> serviceDefinitionIds, final Set<Long> interfaceIds) {
		logger.debug("createBulkAuthorizationIntraCloudResponse started...");
		final List<AuthorizationIntraCloud> entries = createBulkAuthorizationIntraCloud(consumerId, providerIds, serviceDefinitionIds, interfaceIds);
		final Page<AuthorizationIntraCloud> entryPage = new PageImpl<>(entries);
		
		return DTOConverter.convertAuthorizationIntraCloudListToAuthorizationIntraCloudListResponseDTO(entryPage);
	}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudCheckResponseDTO checkAuthorizationIntraCloudRequest(final long consumerId, final long serviceDefinitionId, final Set<IdIdListDTO> providerIdsWithInterfaceIds) {
		logger.debug("checkAuthorizationIntraCloudRequest started...");
				
		try {
			final boolean isConsumerIdInvalid = consumerId < 1 || !systemRepository.existsById(consumerId);
			final boolean isServiceDefinitionIdInvalid = serviceDefinitionId < 1 || !serviceDefinitionRepository.existsById(serviceDefinitionId);
			final boolean isProviderListEmpty = providerIdsWithInterfaceIds == null || providerIdsWithInterfaceIds.isEmpty();
			if (isConsumerIdInvalid || isServiceDefinitionIdInvalid || isProviderListEmpty) {
				String exceptionMsg = "Following parameters are invalid:";
				exceptionMsg = isConsumerIdInvalid ? exceptionMsg + " consumer id," : exceptionMsg;
				exceptionMsg = isServiceDefinitionIdInvalid ? exceptionMsg + " serviceDefinition id," : exceptionMsg;
				exceptionMsg = isProviderListEmpty ? exceptionMsg + " empty providerId list," : exceptionMsg;
				exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1);
				
				throw new InvalidParameterException(exceptionMsg);
			}
			
			final List<IdIdListDTO> authorizedProvidersWithInterfaces = new ArrayList<>(providerIdsWithInterfaceIds.size());
			for (final IdIdListDTO providerWithInterfaces : providerIdsWithInterfaceIds) {
				final Long providerId = providerWithInterfaces.getId();
				if (providerId == null || providerId < 1 || !systemRepository.existsById(providerId)) {
					logger.debug("Invalid provider id: {}", providerId);
				} else {
					final Optional<AuthorizationIntraCloud> authIntraOpt = authorizationIntraCloudRepository.findByConsumerIdAndProviderIdAndServiceDefinitionId(consumerId, providerId,
																																							 serviceDefinitionId);
					if (authIntraOpt.isPresent()) {
						final Set<AuthorizationIntraCloudInterfaceConnection> interfaceConnections = authIntraOpt.get().getInterfaceConnections();
						final List<Long> authorizedInterfaces = new ArrayList<>();
						for (final Long interfaceId : providerWithInterfaces.getIdList()) {
							for (final AuthorizationIntraCloudInterfaceConnection connection : interfaceConnections) {
								if (connection.getServiceInterface().getId() == interfaceId) {
									authorizedInterfaces.add(interfaceId);
								}
							}
						}
						if (!authorizedInterfaces.isEmpty()) {
							authorizedProvidersWithInterfaces.add(new IdIdListDTO(providerId, authorizedInterfaces));
						}
					}
				}
			}
			
			if (authorizedProvidersWithInterfaces.isEmpty()) {
				logger.debug("Have no any authorization intra cloud rule");
			}			
			
			return new AuthorizationIntraCloudCheckResponseDTO(consumerId, serviceDefinitionId, authorizedProvidersWithInterfaces);
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeAuthorizationInterCloudEntryById(final long id) {
		logger.debug("removeAuthorizationInterCloudEntryById started...");
		
		try {
			if (!authorizationInterCloudRepository.existsById(id)) {
				throw new InvalidParameterException("AuthorizationInterCloud with id of '" + id + "' not exists");
			}
			
			authorizationInterCloudRepository.deleteById(id);
			authorizationInterCloudRepository.flush();
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudCheckResponseDTO checkAuthorizationInterCloudResponse(final long cloudId,	final long serviceDefinitionId, final List<IdIdListDTO> providerIdsWithInterfaceIds) {	
		logger.debug("checkAuthorizationInterCloudResponse started...");
		
		try {
			final boolean isCloudIdInvalid = cloudId < 1 || !cloudRepository.existsById(cloudId);
			final boolean isServiceDefinitionIdInvalid = serviceDefinitionId < 1 || !serviceDefinitionRepository.existsById(serviceDefinitionId);
			final boolean isProviderIdsWithInterfaceIdsListInvalid = providerIdsWithInterfaceIds == null ||  providerIdsWithInterfaceIds.isEmpty();
			if (isCloudIdInvalid || isServiceDefinitionIdInvalid || isProviderIdsWithInterfaceIdsListInvalid) {
				String exceptionMsg = "Following parameters are invalid:";
				exceptionMsg = isCloudIdInvalid ? exceptionMsg + " cloudId," : exceptionMsg;
				exceptionMsg = isServiceDefinitionIdInvalid ? exceptionMsg + " serviceDefinitionId," : exceptionMsg;
				exceptionMsg = isProviderIdsWithInterfaceIdsListInvalid ? exceptionMsg + " providerIdsWithInterfaceIds," : exceptionMsg;
				exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1);
				
				throw new InvalidParameterException(exceptionMsg);
			}
			
			final List<IdIdListDTO> authorizedProvidersWithInterfaces = new ArrayList<>(providerIdsWithInterfaceIds.size());
			for (final IdIdListDTO providerWithInterfaces : providerIdsWithInterfaceIds) {
				final Long providerId = providerWithInterfaces.getId();
				if (providerId == null || providerId < 1 || !systemRepository.existsById(providerId)) {
					logger.debug("Invalid provider id: {}", providerId);
				} else {
					final Optional<AuthorizationInterCloud> authInterOpt = authorizationInterCloudRepository.findByCloudIdAndProviderIdAndServiceDefinitionId(cloudId, providerId, serviceDefinitionId);
					
					if (authInterOpt.isPresent()) {
						final Set<AuthorizationInterCloudInterfaceConnection> interfaceConnections = authInterOpt.get().getInterfaceConnections();
						final List<Long> authorizedInterfaces = new ArrayList<>();
						for (final Long interfaceId : providerWithInterfaces.getIdList()) {
							for (final AuthorizationInterCloudInterfaceConnection connection : interfaceConnections) {
								if (connection.getServiceInterface().getId() == interfaceId) {
									authorizedInterfaces.add(interfaceId);
								}
							}
						}
						if (!authorizedInterfaces.isEmpty()) {
							authorizedProvidersWithInterfaces.add(new IdIdListDTO(providerId, authorizedInterfaces));
						}						
					}
				}
			}
			
			if (authorizedProvidersWithInterfaces.isEmpty()) {
				logger.debug("Have no any authorization inter cloud rule");
			}
			
			return new AuthorizationInterCloudCheckResponseDTO(cloudId, serviceDefinitionId, authorizedProvidersWithInterfaces);
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void checkConstraintsOfAuthorizationIntraCloudTable(final System consumer, final System provider, final ServiceDefinition serviceDefinition) {
		logger.debug("checkConstraintsOfAuthorizationIntraCloudTable started...");
		
		final Optional<AuthorizationIntraCloud> optional = authorizationIntraCloudRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(consumer, provider, serviceDefinition);
		if (optional.isPresent()) {
			throw new InvalidParameterException("AuthorizationIntraCloud entry with consumer id " +  consumer.getId() + ", provider id " + provider.getId() + " and serviceDefinitionId " +
												serviceDefinition.getId() + " already exists");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<AuthorizationIntraCloud> createBulkAuthorizationIntraCloudWithOneProviderAndMoreServiceDefinitionAndOneInterface(final System consumer, final Long providerId,
																												   final Set<Long> serviceDefinitionIds, final Long interfaceId) {
		logger.debug("createBulkAuthorizationIntraCloudWithOneProviderAndMoreServiceDefinitionAndOneInterface started...");
		
		final Optional<ServiceInterface> interfaceOpt = serviceInterfaceRepository.findById(interfaceId);
		if (interfaceOpt.isEmpty()) {
			throw new InvalidParameterException("ServiceInterface with id of " + interfaceId + " not exists");
		}
		final ServiceInterface serviceInterface = interfaceOpt.get();
		
		final Optional<System> providerOpt = systemRepository.findById(providerId);		
		if (providerOpt.isPresent()) {
			final System provider = providerOpt.get();

			final List<AuthorizationIntraCloud> toBeSaved = new ArrayList<>(serviceDefinitionIds.size());
			for (final Long serviceId : serviceDefinitionIds) {
				final Optional<ServiceDefinition> serviceOpt = serviceDefinitionRepository.findById(serviceId);
				if (serviceOpt.isPresent()) {
					final ServiceDefinition service = serviceOpt.get();
					try {
						checkConstraintsOfAuthorizationIntraCloudTable(consumer, provider, service);
						toBeSaved.add(new AuthorizationIntraCloud(consumer, provider, service));
					} catch (final InvalidParameterException ex) {
						//not throwing towards as in this bulk operation case should be only a warning
						logger.debug(ex.getMessage());
					}
				} else {
					throw new InvalidParameterException("ServiceDefinition with id of " + serviceId + " not exists");
				}
			}
			
			final List<AuthorizationIntraCloud> authIntraEntries = authorizationIntraCloudRepository.saveAll(toBeSaved);
			for (final AuthorizationIntraCloud authIntraEntry : authIntraEntries) {
				final AuthorizationIntraCloudInterfaceConnection connection = authorizationIntraCloudInterfaceConnectionRepository.save(new AuthorizationIntraCloudInterfaceConnection(authIntraEntry, serviceInterface));
				authIntraEntry.getInterfaceConnections().add(connection);
			}
			authorizationIntraCloudInterfaceConnectionRepository.flush();

			final List<AuthorizationIntraCloud> savedAuthIntraEntries = authorizationIntraCloudRepository.saveAll(authIntraEntries);
			authorizationIntraCloudRepository.flush();
			return savedAuthIntraEntries;
			
		} else {
			throw new InvalidParameterException("Provider system with id of " + providerId + " not exists");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<AuthorizationIntraCloud> createBulkAuthorizationIntraCloudWithOneServiceDefinitionAndMoreProviderAndMoreInterface(final System consumer, final Set<Long> providerIds, final Long serviceId, final Set<Long> interfaceIds) {
		logger.debug("createBulkAuthorizationIntraCloudWithOneServiceDefinitionAndMoreProviderAndMoreInterface started...");
		
		final Optional<ServiceDefinition> serviceOpt = serviceDefinitionRepository.findById(serviceId);
		if (serviceOpt.isPresent()) {
			final ServiceDefinition service = serviceOpt.get();
			
			final List<ServiceInterface> interfaces = new ArrayList<>(interfaceIds.size());
			for (final Long id : interfaceIds) {
				final Optional<ServiceInterface> interfaceOpt = serviceInterfaceRepository.findById(id);
				if (interfaceOpt.isPresent()) {
					interfaces.add(interfaceOpt.get());
				} else {
					logger.debug("ServiceInterface with id of '{}' not exsists", id);
				}
			}
			if (interfaces.isEmpty()) {
				throw new InvalidParameterException("interfaceId list doesn't contain any existing ServiceInterface");
			}

			final List<AuthorizationIntraCloud> toBeSaved = new ArrayList<>(providerIds.size());
			for (final Long providerId : providerIds) {
				final Optional<System> providerOpt = systemRepository.findById(providerId);
				if (providerOpt.isPresent()) {
					final System provider = providerOpt.get();
					try {
						checkConstraintsOfAuthorizationIntraCloudTable(consumer, provider, service);
						toBeSaved.add(new AuthorizationIntraCloud(consumer, provider, service));
					} catch (final InvalidParameterException ex) {
						//not throwing towards as in this bulk operation case should be only a warning
						logger.debug(ex.getMessage());
					}
				} else {
					throw new InvalidParameterException("Provider system with id of " + providerId + " not exists");
				}
			}
			
			final List<AuthorizationIntraCloud> authIntraEntries = authorizationIntraCloudRepository.saveAll(toBeSaved);
			for (final AuthorizationIntraCloud authIntraEntry : authIntraEntries) {
				for (final ServiceInterface serviceInterface : interfaces) {
					final AuthorizationIntraCloudInterfaceConnection connection = authorizationIntraCloudInterfaceConnectionRepository.save(new AuthorizationIntraCloudInterfaceConnection(authIntraEntry, serviceInterface));
					authIntraEntry.getInterfaceConnections().add(connection);
				}
			}
			authorizationIntraCloudInterfaceConnectionRepository.flush();
			
			final List<AuthorizationIntraCloud> savedAuthIntraEntries = authorizationIntraCloudRepository.saveAll(authIntraEntries);
			authorizationIntraCloudRepository.flush();
			return savedAuthIntraEntries;
			
		} else {
			throw new InvalidParameterException("ServiceDefinition with id of " + serviceId + " not exists");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkConstraintsOfAuthorizationInterCloudTable(final Cloud cloud, final System provider, final ServiceDefinition serviceDefinition) {
		logger.debug("checkConstraintsOfAuthorizationInterCloudTable started...");
		
		final Optional<AuthorizationInterCloud> optional = authorizationInterCloudRepository.findByCloudAndProviderAndServiceDefinition(cloud, provider, serviceDefinition);
		if (optional.isPresent()) {
			throw new InvalidParameterException("AuthorizationInterCloud entry with this cloudId: " +  cloud.getId()  + " and providerId: " + provider.getId() + " and serviceDefinition :" + serviceDefinition.getServiceDefinition() + 
												" already exists");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<AuthorizationInterCloud> createBulkAuthorizationInterCloudWithOneProviderAndMoreServiceDefinitionAndOneInterface(final Cloud cloud, final Long providerId, final Set<Long> serviceDefinitionIds, final Long interfaceId) {
		logger.debug("createBulkAuthorizationInterCloudWithOneProviderAndMoreServiceDefinitionAndOneInterface started...");
		
		final Optional<ServiceInterface> interfaceOpt = serviceInterfaceRepository.findById(interfaceId);
		if (interfaceOpt.isEmpty()) {
			throw new InvalidParameterException("ServiceInterface with id of " + interfaceId + " not exists");
		}
		final ServiceInterface serviceInterface = interfaceOpt.get();
		
		final Optional<System> providerOpt = systemRepository.findById(providerId);
		if (providerOpt.isPresent()) {
			final System provider = providerOpt.get();
			
			final List<AuthorizationInterCloud> toBeSaved = new ArrayList<>(serviceDefinitionIds.size());
			for (final Long serviceId : serviceDefinitionIds) {
				final Optional<ServiceDefinition> serviceOpt = serviceDefinitionRepository.findById(serviceId);
				if (serviceOpt.isPresent()) {
					final ServiceDefinition service = serviceOpt.get();
					try {
						checkConstraintsOfAuthorizationInterCloudTable(cloud, provider, service);
						toBeSaved.add(new AuthorizationInterCloud(cloud, provider, service));
					} catch (final InvalidParameterException ex) {
						//not throwing towards as in this bulk operation case should be only a warning
						logger.debug(ex.getMessage());
					}
					
				} else {
					throw new InvalidParameterException("ServiceDefinition with id of " + serviceId + " not exists");
				}				
			}
			
			final List<AuthorizationInterCloud> authInterEntries = authorizationInterCloudRepository.saveAll(toBeSaved);
			for (final AuthorizationInterCloud authInterEntry: authInterEntries) {
				final AuthorizationInterCloudInterfaceConnection connection = authorizationInterCloudInterfaceConnectionRepository.save(new AuthorizationInterCloudInterfaceConnection(authInterEntry, serviceInterface));
				authInterEntry.getInterfaceConnections().add(connection);
			}
			authorizationInterCloudInterfaceConnectionRepository.flush();
			
			final List<AuthorizationInterCloud> savedAuthInterEntries = authorizationInterCloudRepository.saveAll(authInterEntries);
			authorizationInterCloudRepository.flush();
			return savedAuthInterEntries;
			
		} else {
			throw new InvalidParameterException("Provider system with id of " + providerId + " not exists");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<AuthorizationInterCloud> createBulkAuthorizationInterCloudWithOneServiceDefinitionAndMoreProviderAndMoreInterface(final Cloud cloud, final Set<Long> providerIds, final Long serviceId, final Set<Long> interfaceIds) {
		logger.debug("createBulkAuthorizationInterCloudWithOneServiceDefinitionAndMoreProviderAndMoreInterface started...");
		
		final Optional<ServiceDefinition> serviceOpt = serviceDefinitionRepository.findById(serviceId);
		if (serviceOpt.isPresent()) {
			final ServiceDefinition service = serviceOpt.get();
			
			final List<ServiceInterface> interfaces = new ArrayList<>(interfaceIds.size());
			for (final Long id : interfaceIds) {
				final Optional<ServiceInterface> interfaceOpt = serviceInterfaceRepository.findById(id);
				if (interfaceOpt.isPresent()) {
					interfaces.add(interfaceOpt.get());
				} else {
					logger.debug("ServiceInterface with id of '{}' not exsists", id);
				}				
			}
			if (interfaces.isEmpty()) {
				throw new InvalidParameterException("interfaceId list doesn't contain any existing ServiceInterface");
			}
			
			final List<AuthorizationInterCloud> toBeSaved = new ArrayList<>(providerIds.size());
			for (final Long providerId : providerIds) {
				final Optional<System> providerOpt = systemRepository.findById(providerId);
				if (providerOpt.isPresent()) {
					final System provider = providerOpt.get();
					try {
						checkConstraintsOfAuthorizationInterCloudTable(cloud, provider, service);
						toBeSaved.add(new AuthorizationInterCloud(cloud, provider, service));
					} catch (final InvalidParameterException ex) {
						//not throwing towards as in this bulk operation case should be only a warning
						logger.debug(ex.getMessage());
					}
				} else {
					throw new InvalidParameterException("Provider system with id of " + providerId + " not exists");
				}
			}
			
			final List<AuthorizationInterCloud> authInterEntries = authorizationInterCloudRepository.saveAll(toBeSaved);
			for (final AuthorizationInterCloud authInterEntry : authInterEntries) {
				for (final ServiceInterface serviceInterface : interfaces) {
					final AuthorizationInterCloudInterfaceConnection connection = authorizationInterCloudInterfaceConnectionRepository.save(new AuthorizationInterCloudInterfaceConnection(authInterEntry, serviceInterface));
					authInterEntry.getInterfaceConnections().add(connection);
				}
			}
			authorizationInterCloudInterfaceConnectionRepository.flush();
			
			final List<AuthorizationInterCloud> savedAuthInterEntries = authorizationInterCloudRepository.saveAll(authInterEntries);
			authorizationInterCloudRepository.flush();
			return savedAuthInterEntries;
			
		} else {
			throw new InvalidParameterException("ServiceDefinition with id of " + serviceId + " not exists");
		}
		
		
	}	
	
}