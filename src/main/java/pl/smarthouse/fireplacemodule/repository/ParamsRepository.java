package pl.smarthouse.fireplacemodule.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import pl.smarthouse.fireplacemodule.model.dao.FireplaceModuleParamsDao;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ParamsRepository {
  private final ReactiveMongoTemplate reactiveMongoTemplate;

  public Mono<FireplaceModuleParamsDao> saveParams(
      final FireplaceModuleParamsDao fireplaceModuleParamsDao, final String paramTableName) {
    return reactiveMongoTemplate
        .remove(new Query(), FireplaceModuleParamsDao.class, paramTableName)
        .then(reactiveMongoTemplate.save(fireplaceModuleParamsDao, paramTableName));
  }

  public Mono<FireplaceModuleParamsDao> getParams(final String paramTableName) {
    return reactiveMongoTemplate
        .findAll(FireplaceModuleParamsDao.class, paramTableName)
        .last()
        .cache(Duration.ofMinutes(1));
  }
}
