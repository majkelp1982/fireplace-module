package pl.smarthouse.fireplacemodule.service;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.fireplacemodule.model.dao.FireplaceModuleParamsDao;
import pl.smarthouse.fireplacemodule.repository.ParamsRepository;
import pl.smarthouse.sharedobjects.dto.fireplace.FireplaceModuleParamsDto;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class FireplaceModuleParamsService {
  private final ParamsRepository paramsRepository;
  private final FireplaceModuleService fireplaceModuleService;
  private final ModelMapper modelMapper = new ModelMapper();
  private FireplaceModuleParamsDto fireplaceModuleParamsDto;

  public Mono<FireplaceModuleParamsDto> saveParams(
      final FireplaceModuleParamsDto fireplaceModuleParamsDto) {
    return getParamTableName()
        .flatMap(
            paramTableName ->
                paramsRepository.saveParams(
                    modelMapper.map(fireplaceModuleParamsDto, FireplaceModuleParamsDao.class),
                    paramTableName))
        .thenReturn(fireplaceModuleParamsDto);
  }

  private Mono<String> getParamTableName() {
    return Mono.just(fireplaceModuleService.getModuleName())
        .map(moduleName -> moduleName.toLowerCase() + "_settings");
  }

  public FireplaceModuleParamsDto getParams() {
    if (fireplaceModuleParamsDto == null) {
      refreshParams();
    }
    while (fireplaceModuleParamsDto == null) {}
    return fireplaceModuleParamsDto;
  }

  @Scheduled(initialDelay = 5000, fixedDelay = 60 * 1000)
  private void refreshParams() {
    getParamTableName()
        .flatMap(
            paramTableName ->
                paramsRepository
                    .getParams(paramTableName)
                    .doOnNext(
                        fireplaceModuleParamsDao ->
                            log.debug("Successfully retrieve params: {}", fireplaceModuleParamsDao))
                    .map(
                        fireplaceModuleParamsDao ->
                            fireplaceModuleParamsDto =
                                modelMapper.map(
                                    fireplaceModuleParamsDao, FireplaceModuleParamsDto.class))
                    .onErrorResume(
                        NoSuchElementException.class,
                        throwable -> {
                          log.warn("No params found for: {}", paramTableName);
                          return Mono.empty();
                        })
                    .doOnError(
                        throwable ->
                            log.error(
                                "Error on get params. Error message: {}, Error: {}",
                                throwable.getMessage(),
                                throwable))
                    .doOnSubscribe(
                        subscription ->
                            log.debug("Get module params from collection: {}", paramTableName)))
        .subscribe();
  }
}
