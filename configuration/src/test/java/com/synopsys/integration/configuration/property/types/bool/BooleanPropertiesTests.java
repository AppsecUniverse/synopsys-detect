package com.synopsys.integration.configuration.property.types.bool;

import static com.synopsys.integration.configuration.util.ConfigTestUtils.configOf;

import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.common.util.Bds;
import com.synopsys.integration.configuration.config.InvalidPropertyException;
import com.synopsys.integration.configuration.config.PropertyConfiguration;
import com.synopsys.integration.configuration.property.PropertyTestHelpUtil;
import com.synopsys.integration.configuration.property.base.NullableProperty;
import com.synopsys.integration.configuration.property.base.ValuedListProperty;
import com.synopsys.integration.configuration.property.base.ValuedProperty;

// Simple glue sanity tests. Theoretically if Config is well tested and Parser is well tested, these will pass so they are not exhaustive.
class BooleanPropertiesTests {
    @Test
    public void testNullable() throws InvalidPropertyException {
        NullableProperty<Boolean> property = new NullableBooleanProperty("boolean.nullable");
        PropertyConfiguration config = configOf(Pair.of("boolean.nullable", "true"));
        Assertions.assertEquals(Optional.of(true), config.getValue(property));

        PropertyTestHelpUtil.assertAllHelpValid(property);

    }

    @Test
    public void testValued() throws InvalidPropertyException {
        ValuedProperty<Boolean> property = new BooleanProperty("boolean.valued", false);
        PropertyConfiguration config = configOf(Pair.of("boolean.valued", "true"));
        Assertions.assertEquals(true, config.getValue(property));

        PropertyTestHelpUtil.assertAllHelpValid(property);
    }

    @Test
    public void testList() throws InvalidPropertyException {
        ValuedListProperty<Boolean> property = new BooleanListProperty("boolean.list", Collections.emptyList());
        PropertyConfiguration config = configOf(Pair.of("boolean.list", "true, true"));
        Assertions.assertEquals(Bds.listOf(true, true), config.getValue(property));

        PropertyTestHelpUtil.assertAllListHelpValid(property);
    }
}