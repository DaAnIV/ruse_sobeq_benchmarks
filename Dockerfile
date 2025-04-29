FROM openjdk:11

# Env variables
ENV SCALA_VERSION=2.13.10
ENV SBT_VERSION=1.6.2
ENV USER_ID=1001
ENV GROUP_ID=1001

# Install dependencies
RUN \
  apt-get update && \
  apt-get install -y curl git rpm && \
  rm -rf /var/lib/apt/lists/*
 
# Install sbt
RUN \
  curl -fsL --show-error "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" | tar xfz - -C /usr/share && \
  chown -R root:root /usr/share/sbt && \
  chmod -R 755 /usr/share/sbt && \
  ln -s /usr/share/sbt/bin/sbt /usr/local/bin/sbt

# Install Scala
RUN \
  case $SCALA_VERSION in \
    2.*) URL=https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz SCALA_DIR=/usr/share/scala-$SCALA_VERSION ;; \
    *) URL=https://github.com/scala/scala3/releases/download/$SCALA_VERSION/scala3-$SCALA_VERSION.tar.gz SCALA_DIR=/usr/share/scala3-$SCALA_VERSION ;; \
  esac && \
  curl -fsL --show-error $URL | tar xfz - -C /usr/share/ && \
  mv $SCALA_DIR /usr/share/scala && \
  chown -R root:root /usr/share/scala && \
  chmod -R 755 /usr/share/scala && \
  ln -s /usr/share/scala/bin/* /usr/local/bin && \
  mkdir -p /test && \
  case $SCALA_VERSION in \
    2*) echo "println(util.Properties.versionMsg)" > /test/test.scala ;; \
    *) echo 'import java.io.FileInputStream;import java.util.jar.JarInputStream;val scala3LibJar = classOf[CanEqual[_, _]].getProtectionDomain.getCodeSource.getLocation.toURI.getPath;val manifest = new JarInputStream(new FileInputStream(scala3LibJar)).getManifest;val ver = manifest.getMainAttributes.getValue("Implementation-Version");@main def main = println(s"Scala version ${ver}")' > /test/test.scala ;; \
  esac && \
  scala -nocompdaemon test/test.scala && \
  rm -fr test
  
# Ant for FrAngel
ENV ANT_VERSION=1.10.14
ENV ANT_HOME=/opt/ant
WORKDIR /tmp
RUN wget --no-check-certificate --no-cookies http://archive.apache.org/dist/ant/binaries/apache-ant-${ANT_VERSION}-bin.tar.gz \
    && wget --no-check-certificate --no-cookies http://archive.apache.org/dist/ant/binaries/apache-ant-${ANT_VERSION}-bin.tar.gz.sha512 \
    && echo "$(cat apache-ant-${ANT_VERSION}-bin.tar.gz.sha512) apache-ant-${ANT_VERSION}-bin.tar.gz" | sha512sum -c \
    && tar -zvxf apache-ant-${ANT_VERSION}-bin.tar.gz -C /opt/ \
    && ln -s /opt/apache-ant-${ANT_VERSION} /opt/ant \
    && rm -f apache-ant-${ANT_VERSION}-bin.tar.gz \
    && rm -f apache-ant-${ANT_VERSION}-bin.tar.gz.sha512

# add executables to path
RUN update-alternatives --install "/usr/bin/ant" "ant" "/opt/ant/bin/ant" 1 && \
    update-alternatives --set "ant" "/opt/ant/bin/ant" 




# Add and use user sobeq
RUN groupadd --gid $GROUP_ID sobeq && useradd -m --gid $GROUP_ID --uid $USER_ID sobeq --shell /bin/bash
USER sobeq

# Switch working directory
WORKDIR /home/sobeq

# Prepare sbt (warm cache)
RUN \
  sbt --script-version && \
  mkdir -p project && \
  echo "scalaVersion := \"${SCALA_VERSION}\"" > build.sbt && \
  echo "sbt.version=${SBT_VERSION}" > project/build.properties && \
  echo "// force sbt compiler-bridge download" > project/Dependencies.scala && \
  echo "case object Temp" > Temp.scala && \
  sbt compile && \
  rm -r project && rm build.sbt && rm Temp.scala && rm -r target

# All set, start setting up SObEq
COPY . .

WORKDIR /home/sobeq/sobeq-main
RUN sbt assembly

WORKDIR /home/sobeq/classical-enumeration
RUN sbt assembly

WORKDIR /home/sobeq/concrete-states
RUN sbt assembly

WORKDIR /home/sobeq/frangel-comparison/lib/FrAngel/
RUN ant
WORKDIR /home/sobeq/frangel-comparison/
RUN mkdir -p "./out/production/frangel_benchmarks"

RUN \
  javac -cp "lib/*:lib/FrAngel/lib/*:lib/FrAngel/frangel.jar" -d "out/production/frangel_benchmarks" src/main/java/Main.java src/main/java/grammar/*.java src/main/java/utils/*.java

WORKDIR /home/sobeq
CMD ["bash"]