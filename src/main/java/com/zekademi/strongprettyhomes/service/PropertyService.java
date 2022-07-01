package com.zekademi.strongprettyhomes.service;

import com.zekademi.strongprettyhomes.domain.*;
import com.zekademi.strongprettyhomes.dto.PropertyDTO;
import com.zekademi.strongprettyhomes.exception.BadRequestException;
import com.zekademi.strongprettyhomes.exception.ResourceNotFoundException;
import com.zekademi.strongprettyhomes.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final AgentRepository agentRepository;
    private final PropertyDetailRepository propertyDetailRepository;
    private final LikeRepository likeRepository;


    private final static String PROPERTY_NOT_FOUND_MSG = "property with id %d not found";
    private final static String AGENT_NOT_FOUND_MSG = "agent with id %d not found";


    public List<PropertyDTO> fetchAllProperties() {

        return propertyRepository.findAllProperty();
    }

    public PropertyDTO findById(Long id) throws ResourceNotFoundException {

        Property property = propertyRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format(PROPERTY_NOT_FOUND_MSG, id)));

        Long totalCount = property.getVisitCount();
        property.setVisitCount(totalCount + 1);
        propertyRepository.save(property);

        return propertyRepository.findPropertyByIdx(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format(PROPERTY_NOT_FOUND_MSG, id)));
    }

    public void add(Property property, Agent agentId, Long detailId) throws BadRequestException {

        property.setVisitCount(0L);

        Agent agent = agentRepository.findById(agentId.getId()).orElseThrow(() ->
                new ResourceNotFoundException(String.format(AGENT_NOT_FOUND_MSG, agentId.getId())));
        property.setAgent(agent);

        PropertyDetail propertyDetail = propertyDetailRepository.findById(detailId).orElseThrow(() ->
                new ResourceNotFoundException(String.format(AGENT_NOT_FOUND_MSG, detailId)));
        Set<PropertyDetail> details = new HashSet<>();
        details.add(propertyDetail);
        property.setPropertyDetails(details);

        propertyRepository.save(property);
    }

    public void updateProperty(Long id, Property property, Long agentId, Long detailId) throws BadRequestException {
        property.setId(id);

        Agent agent = agentRepository.findById(agentId).get();
        PropertyDetail propertyDetail = propertyDetailRepository.findById(detailId).get();

        Set<PropertyDetail> propertyDetails = new HashSet<PropertyDetail>();
        propertyDetails.add(propertyDetail);

        property.setPropertyDetails(propertyDetails);
        property.setAgent(agent);

        propertyRepository.save(property);
    }

    public void removeById(Long id) throws ResourceNotFoundException {
     
        boolean propertyExist = propertyRepository.existsById(id);
        if (!propertyExist) throw new ResourceNotFoundException("property does not exist");

        propertyRepository.deleteById(id);

    }
    
    public Long setLike(Long propertyId, Long userId) {

        Property property = propertyRepository.findById(propertyId).orElseThrow(() ->
                new ResourceNotFoundException("Property not found"));
        if (likeRepository.existsByPropertyIdAndUserId(propertyId, userId)) {
            Like like = likeRepository.findByPropertyIdAndUserId(propertyId, userId);

            if (like.getIsLiked() == false) {
                like.setIsLiked(true);
                Long decreaseLike = property.getLikeCount() - 1;
                property.setLikeCount(decreaseLike);
            } else {
                like.setIsLiked(false);
                Long increaseLike = property.getLikeCount() + 1;
                property.setLikeCount(increaseLike);
            }
            propertyRepository.save(property);
            likeRepository.save(like);
        } else {
            Like like = new Like();
            like.setPropertyId(propertyId);
            like.setUserId(userId);
            like.setIsLiked(false);
            Long increaseLike = property.getLikeCount() + 1;
            property.setLikeCount(increaseLike);
            propertyRepository.save(property);
            propertyRepository.save(property);
            likeRepository.save(like);
        }
        return property.getLikeCount();
    }

}
