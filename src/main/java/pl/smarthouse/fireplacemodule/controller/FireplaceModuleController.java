package pl.smarthouse.fireplacemodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.smarthouse.fireplacemodule.service.FireplaceModuleParamsService;
import pl.smarthouse.fireplacemodule.service.FireplaceModuleService;
import pl.smarthouse.sharedobjects.dto.fireplace.FireplaceModuleDto;
import pl.smarthouse.sharedobjects.dto.fireplace.FireplaceModuleParamsDto;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.State;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class FireplaceModuleController {
  private final FireplaceModuleService fireplaceModuleService;
  private final FireplaceModuleParamsService fireplaceModuleParamsService;

  @GetMapping("/fireplace")
  public Mono<FireplaceModuleDto> getFireplaceModule() {
    return Mono.just(fireplaceModuleService.getFireplaceModule());
  }

  @PatchMapping("/fireplace/state/toggle")
  public Mono<State> stateToggle() {
    return Mono.just(fireplaceModuleService.stateToggle());
  }

  @PostMapping("/params")
  public Mono<FireplaceModuleParamsDto> saveParams(
      @RequestBody final FireplaceModuleParamsDto fireplaceModuleParamsDto) {
    return fireplaceModuleParamsService.saveParams(fireplaceModuleParamsDto);
  }

  @GetMapping("/params")
  public Mono<FireplaceModuleParamsDto> getParams() {
    return Mono.just(fireplaceModuleParamsService.getParams());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public Mono<String> exceptionHandler(final Exception exception) {
    return Mono.just(exception.getMessage());
  }
}
