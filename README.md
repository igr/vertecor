# VERTECOR v1.2

> Would you like to enter your logs in the Vertec faster? 

## Usage

Download the `jar` from the [releases](https://github.com/igr/vertecor/releases) and run it:

```bash
java -jar vertecor-1.2.jar
```

The **Vertecor** is user friendly: if you don't specify arguments in the command line, it will ask for it. So, you don't have to pass anything; just run the program. However, you may speed up and automate the process by passing few or all required arguments.

Here are some usage scenarios.

### Interactive mode

This is the default mode, when no argument is provided:

```bash
java -jar vertecor.jar
```

**Vertecor** will now aks for all the data. Something like this:

![](v1.png)

The only thing you can't enter like this is a `date` - the current date is assumed.

### Program arguments

As said, you can optionally pass one or more arguments from cli. Arguments can be _parameters_ or _options_.

Options are:

+ `--nocache` - clears the cache before the usage
+ `-d | --date <date>` - specifies the date in ISO format
+ `-m | --message <message>` - specifies the description
+ `-h | --hours <hours>` - the decimal amount of spent hours

There are 3 parameters (they take precedence over the options):

```
<project-phase-type> [<hours> [<message>]]
```

Parameters are:

+ `project-phase-type` - comma-separated IDs for the project, phase and/or service type (notice the `TIP` on above screenshot!)
+ `hours` - decimal value for spent hours
+ `message` - description

Again, you may specify _anything_ you want. Whatever is not defined by command line arguments, the **Vertecor** will ask for.

#### Examples

Specify date and hours, ask for project information and a message:

```bash
java -jar vertecor.jar -d 2018-06-15 -h 2.5
```

Set everything in command line, don't ask for anything more:

```bash
java -jar vertecor.jar 111,222,333 2.5 "Awesome work"
```

Update the cache, pass the hours and description, ask for the project:

```bash
java -jar vertecor.jar -h 1.5 -m "Good job" --nocache
```

You get the point :)

## Some things to be aware of

+ Project related entries are cached. They are fetched very first time and cached locally. This improves the speed. To invalidate the cache, just pass the `--nocache` option.
+ Username & password in stored in the cache folder, too, in plaintext. Yeah.
+ Cache folder is located at `~/.vertec`.
+ Compiled on Java8.
+ You need UTF8 font in console.
+ This was coded in couple of hours.
+ No idea how it looks on Windows :)

## Development notes

It's easy to build:

```bash
./gradlew clean jar
```

## License

BSD-2