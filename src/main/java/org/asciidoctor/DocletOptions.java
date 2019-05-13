package org.asciidoctor;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.StandardDoclet;

public class DocletOptions {
    public DocletOptions(StandardDoclet standardDoclet) {
        this.standardDoclet = standardDoclet;
    }

    public Set<? extends Doclet.Option> getSupportedOptions() {
        Set<Doclet.Option> standardOptions = standardDoclet.getSupportedOptions();
        Map<String,Doclet.Option> optionsMap = new TreeMap<>(standardOptions.stream().collect(Collectors.toMap(option -> option.getNames().get(0), option -> option)));

        // add interceptor for standard doclet option we care about
        optionsMap.put("-d", new DelegatingOption(optionsMap.get("-d")) {
            @Override
            protected void process(List<String> arguments) {
                destdir =  Optional.of(new File(arguments.get(0)));
            }
        });

        TreeSet<Doclet.Option> options = new TreeSet<>(Comparator.comparing(o -> o.getNames().get(0)));
        options.addAll(optionsMap.values());
        return options;
    }

    public Optional<File> destDir() {
        return destdir;
    }


    private abstract class DelegatingOption implements Doclet.Option {
        protected DelegatingOption(Doclet.Option delegate) {
            if (delegate == null) throw new NullPointerException("delegate is null");
            this.delegate = delegate;
        }

        @Override
        public final List<String> getNames() {
            return delegate.getNames();
        }

        @Override
        public final String getDescription() {
            return delegate.getDescription();
        }

        @Override
        public final int getArgumentCount() {
            return delegate.getArgumentCount();
        }

        @Override
        public final String getParameters() {
            return delegate.getParameters();
        }

        @Override
        public final Kind getKind() {
            return delegate.getKind();
        }

        @Override
        public final boolean process(String option, List<String> arguments) {
            process(arguments);
            return delegate.process(option, arguments);
        }

        protected abstract void process(List<String> arguments);

        private final Doclet.Option delegate;
    }

    private final StandardDoclet standardDoclet;
    private Optional<File> destdir = Optional.empty();
}
