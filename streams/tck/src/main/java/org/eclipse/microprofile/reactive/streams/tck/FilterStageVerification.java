/*******************************************************************************
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.eclipse.microprofile.reactive.streams.tck;

import org.eclipse.microprofile.reactive.streams.CompletionBuilder;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class FilterStageVerification extends AbstractStageVerification {

  FilterStageVerification(ReactiveStreamsTck.VerificationDeps deps) {
    super(deps);
  }

  @Test
  public void filterStageShouldFilterElements() {
    assertEquals(await(ReactiveStreams.of(1, 2, 3, 4, 5, 6)
        .filter(i -> (i & 1) == 1)
        .toList()
        .run(getEngine())), Arrays.asList(1, 3, 5));
  }

  @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "failed")
  public void filterStageShouldPropagateRuntimeExceptions() {
    await(ReactiveStreams.of("foo")
        .filter(foo -> {
          throw new RuntimeException("failed");
        })
        .toList()
        .run(getEngine()));
  }

  @Test
  public void filterStageShouldSupportSkip() {
    assertEquals(await(ReactiveStreams.of(1, 2, 3, 4)
        .skip(2)
        .toList()
        .run(getEngine())), Arrays.asList(3, 4));
  }

  @Test
  public void filterStageShouldSupportDropWhile() {
    assertEquals(await(ReactiveStreams.of(1, 2, 3, 4)
        .dropWhile(i -> i < 3)
        .toList()
        .run(getEngine())), Arrays.asList(3, 4));
  }

  @Test
  public void filterStageShouldInstantiatePredicateOncePerRun() {
    CompletionBuilder<List<Integer>> completion =
        ReactiveStreams.of(1, 2, 3, 4, 5, 6)
            .skip(3)
            .toList();

    assertEquals(await(completion.run(getEngine())), Arrays.asList(4, 5, 6));
    assertEquals(await(completion.run(getEngine())), Arrays.asList(4, 5, 6));
  }


  @Override
  List<Object> reactiveStreamsTckVerifiers() {
    return Collections.singletonList(
        new ProcessorVerification()
    );
  }

  class ProcessorVerification extends StageProcessorVerification<Integer> {

    @Override
    public Processor<Integer, Integer> createIdentityProcessor(int bufferSize) {
      return ReactiveStreams.<Integer>builder().filter(i -> true).buildRs(getEngine());
    }

    @Override
    public Publisher<Integer> createFailedPublisher() {
      return ReactiveStreams.<Integer>failed(new RuntimeException("failed"))
          .filter(i -> true).buildRs(getEngine());
    }

    @Override
    public Integer createElement(int element) {
      return element;
    }
  }
}
