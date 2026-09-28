# Slot Core Demo

A console application demonstrating the `slot-core` library. It builds a slot field
from physical reel positions, finds winning combinations, and displays their prize
codes. A separate hint handler can suggest positions that place requested symbols
on a configured line.

The library source is available in [slot-core](https://github.com/IhorVS/slot-core).

## Requirements

- JDK 21
- Maven
- Source projects `slot-core` and `slot-core-demo`

The demo depends on `ivs.games.accessories:slot-core:1.0.0`. Its `pom.xml` does not
build `slot-core` automatically.

## Build and run

1. Check that Maven uses JDK 21:

   ```bash
   java -version
   mvn -version
   ```

2. From the `slot-core` project directory, run its tests and install the library
   into your local Maven repository:

   ```bash
   mvn clean install
   ```

3. From the `slot-core-demo` project directory, run its tests and build the demo:

   ```bash
   mvn clean package
   ```

4. Open `slot-core-demo` in your IDE and run
   `ivs.games.accessories.slot.demo.SlotDemoApplication`.

The application loads its YAML configuration and prints the command list at startup.
If you change the `slot-core` API while keeping version `1.0.0`, run
`mvn clean install` in `slot-core` again and reload Maven dependencies in the demo.

The demo POM does not configure an executable JAR with bundled dependencies. Run
the main class from the IDE, which supplies the Maven dependencies and resources
on the classpath.

## Commands

| Command | Alias | Description |
| --- | --- | --- |
| `spin` | `s` | Spin with a random position for every reel. |
| `spin <p1> ... <pN>` | `s <p1> ... <pN>` | Set positions from the leftmost reel onward. Omitted positions are chosen randomly. |
| `hint <symbol,...> line <id>` | `h <symbol,...> line <id>` | Find positions that place the requested symbols on a configured line. |
| `repeat` | `r` | Repeat the last successful `spin` or `hint` with the same arguments. |
| `help` | `?` | Display the command list. |
| `exit` | `q` | Exit the application. `quit` also works. |

For example:

```text
> s 0 1 1 2 0
> h A,A,A line 0
> h WLD, A, SCT line 1
> r
```

Reel positions and line IDs start at zero. Symbol names are case-sensitive. In a
hint, separate symbols with commas; spaces around the commas are allowed. The
requested symbols correspond to consecutive positions on the selected line,
starting with its first reel. You may specify fewer symbols than the line has
positions.

A hint returns the first suitable physical position found for each requested
symbol and prints a ready-to-use `spin` command. If that command specifies fewer
positions than there are reels, the remaining positions are chosen randomly.
Placing symbols on a line does not guarantee a winning combination.

`repeat` prints the command it is about to execute. A successful `spin` or `hint`
becomes the new command to repeat. Invalid commands, `help`, and `repeat` do not
replace the saved command. Repeating a spin without positions generates a new
random spin; repeating a spin with partial positions selects the missing
positions again.

## Spin output

A spin displays the actual positions of all reels, the visible field, and any
wins. Each win includes its combination ID, group, matched symbols, field
positions, and prize codes. Linear wins also include the line ID. When there
are no wins, the application prints `No winning combinations.`

The demo uses the longest combination match policy within a combination group.
Wild symbols participate in linear matching.

## Configure the demo slot

The application loads three resources from `src/main/resources/demoslot/`:

| File | Contents |
| --- | --- |
| `slotfield.yaml` | Visible field height and symbols on each physical reel. |
| `lines.yaml` | Winning lines as row indices from left to right. |
| `combinations.yaml` | Wild symbols, linear and scatter combinations, their IDs, and prize codes. |

To change the slot configuration:

1. Set `fieldHeight` and define the physical reels in `slotfield.yaml`. Each
   reel must contain at least `fieldHeight` symbols. Symbol names must correspond
   to `StandardReelItem` values.
2. Define the lines in `lines.yaml`. Each line must have one row index for every
   reel. Indices start at `0` and must be smaller than `fieldHeight`. A line's
   ID is its zero-based position in the `lines` list.
3. Define `wildSymbols` and the `linear` and `scatter` groups in
   `combinations.yaml`. Each combination needs an `id`, `symbols`, and `prizes`.
   Combination IDs form one continuous sequence starting at `0`: first the
   linear groups, then the scatter groups, in declaration order.
4. Run `mvn clean test` in `slot-core-demo`, then launch the application.

The configuration loader checks each file's structure. When assembling the slot
configuration, it also checks that every line covers the configured reels and
uses rows within the visible field.

### Example configuration files

The following files define a five-reel slot with three visible rows, one
winning line, one linear combination, and one scatter combination. They form
one complete example: replace all three files together if you want to try it.

`slotfield.yaml`:

```yaml
fieldHeight: 3

reels:
  - [ A, K, SCT, WLD ]
  - [ A, Q, SCT, K ]
  - [ A, J, SCT, Q ]
  - [ A, K, SCT, J ]
  - [ A, Q, SCT, K ]
```

Each reel lists its symbols in physical order. Reel position `0` selects the
first symbol as the top visible symbol; subsequent symbols fill the rows below.
Positions wrap around at the end of a reel.

`lines.yaml`:

```yaml
lines:
  - [ 0, 0, 0, 0, 0 ]
```

This line passes through row `0` on all five reels and receives ID `0`.
A line must contain exactly one row index per reel. With `fieldHeight: 3`,
valid row indices are `0`, `1`, and `2`.

`combinations.yaml`:

```yaml
wildSymbols: [ WLD ]

combinations:
  linear:
    A:
      - id: 0
        symbols: [ A, A, A ]
        prizes: [ C10 ]

  scatter:
    SCT:
      - id: 1
        symbols: [ SCT, SCT, SCT ]
        prizes: [ C15, FS3 ]
```

The linear combination is checked along configured lines. The scatter
combination is checked across the entire visible field. Combination IDs form
one sequence across both sections: `0`, then `1` in this example. Prize codes
are displayed in the demo but are not applied to a balance.

### Supplied configuration

The supplied configuration has five reels and three visible rows. Reel positions
are cyclic: the specified position selects the top visible symbol, and
subsequent symbols fill the rows below it.

Three lines are active: top (`0`), middle (`1`), and bottom (`2`). The V-shaped
and other lines shown in comments in `lines.yaml` are examples, not active
lines.

`combinations.yaml` defines linear combinations in group `A` and scatter
combinations in groups `SCT` and `MUL`. `WLD` is the configured wild symbol.

Prize codes are displayed but not paid out by this demo:

- `C<N>` — a fixed prize of *N* tokens.
- `x<N>` — a multiplier of *N*.
- `FS<N>` — *N* free spins.

Applying these prizes belongs to a concrete slot implementation.
