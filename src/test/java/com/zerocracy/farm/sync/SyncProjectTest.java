/**
 * Copyright (c) 2016-2018 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.farm.sync;

import com.jcabi.aspects.Tv;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.pmo.Pmo;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link SyncProject}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class SyncProjectTest {

    @Test
    public void locksFilesIndividually() throws Exception {
        try (final Farm farm = new SyncFarm(new FkFarm())) {
            final Project project = new Pmo(farm);
            final Collection<Item> items = new LinkedList<>();
            for (int idx = 0; idx < Tv.FIFTY; ++idx) {
                final Item item = project.acq(String.format("%d.xml", idx));
                item.path();
                items.add(item);
            }
            for (final Item item : items) {
                item.close();
            }
            MatcherAssert.assertThat(
                items.size(),
                Matchers.greaterThan(0)
            );
        }
    }

    @Test
    @Ignore
    public void workOnHighLoad() throws Exception {
        try (final Farm farm = new SyncFarm(new FkFarm())) {
            final Project project = new Pmo(farm);
            // @checkstyle MagicNumber (1 line)
            for (int num = 0; num < 100000; ++num) {
                project.acq("test.txt");
            }
            TimeUnit.MINUTES.sleep(2L);
            project.acq("test.txt");
        }
    }
}
