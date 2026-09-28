package ivs.games.accessories.slot.demo.console;

import ivs.game.accessories.slot.reel.impl.StandardReelItem;
import ivs.games.accessories.slot.demo.engine.HintHandler;
import ivs.games.accessories.slot.demo.engine.HintResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HintCommandTest {

    @Mock
    private HintHandler hintHandler;

    @Mock
    private ConsoleRenderer renderer;

    private HintCommand command;

    @BeforeEach
    void setUp() {
        command = new HintCommand(hintHandler, renderer);
    }

    @Test
    void executesHintForSingleCharacterSymbols() {
        List<StandardReelItem> symbols = List.of(
                StandardReelItem.A,
                StandardReelItem.A,
                StandardReelItem.A
        );
        HintResult result = new HintResult(symbols, 0, List.of(1, 2, 3));

        when(hintHandler.hint(symbols, 0)).thenReturn(result);

        command.execute(new String[]{"A,A,A", "line", "0"});

        verify(hintHandler).hint(symbols, 0);
        verify(renderer).printHintResult(result);
        verify(renderer, never()).printError(anyString());
    }

    @Test
    void executesHintForMultiCharacterSymbols() {
        List<StandardReelItem> symbols = List.of(
                StandardReelItem.WLD,
                StandardReelItem.A,
                StandardReelItem.SCT
        );
        HintResult result = new HintResult(symbols, 0, List.of(3, 7, 4));

        when(hintHandler.hint(symbols, 0)).thenReturn(result);

        command.execute(new String[]{"WLD,A,SCT", "line", "0"});

        verify(hintHandler).hint(symbols, 0);
        verify(renderer).printHintResult(result);
        verify(renderer, never()).printError(anyString());
    }

    @Test
    void printsUsageForSymbolsSeparatedBySpaces() {
        command.execute(new String[]{"A", "A", "A", "line", "0"});

        verify(renderer).printError("Usage: hint <symbol,...> line <line-id>");
        verifyNoInteractions(hintHandler);
    }

    @Test
    void printsUsageWhenLineKeywordIsMissing() {
        command.execute(new String[]{"A,A,A", "at", "0"});

        verify(renderer).printError("Usage: hint <symbol,...> line <line-id>");
        verifyNoInteractions(hintHandler);
    }

    @Test
    void printsErrorForUnknownSymbol() {
        command.execute(new String[]{"A,UNKNOWN,A", "line", "0"});

        verify(renderer).printError("Unknown symbol: 'UNKNOWN'.");
        verifyNoInteractions(hintHandler);
    }

    @Test
    void printsErrorForEmptySymbolBetweenCommas() {
        command.execute(new String[]{"A,,A", "line", "0"});

        verify(renderer).printError("Symbols must be separated by single commas.");
        verifyNoInteractions(hintHandler);
    }

    @Test
    void printsErrorForInvalidLineId() {
        command.execute(new String[]{"A,A,A", "line", "first"});

        verify(renderer).printError(
                "Invalid line ID: 'first'. Line ID must be an integer."
        );
        verifyNoInteractions(hintHandler);
    }

    @Test
    void printsErrorWhenHandlerRejectsHint() {
        List<StandardReelItem> symbols = List.of(
                StandardReelItem.WLD,
                StandardReelItem.A,
                StandardReelItem.SCT
        );
        IllegalArgumentException exception = new IllegalArgumentException(
                "Symbol SCT was not found on reel 2."
        );

        when(hintHandler.hint(symbols, 0)).thenThrow(exception);

        command.execute(new String[]{"WLD,A,SCT", "line", "0"});

        verify(hintHandler).hint(symbols, 0);
        verify(renderer).printError("Symbol SCT was not found on reel 2.");
        verify(renderer, never()).printHintResult(any());
    }

    @Test
    void executesHintWithSpacesBetweenSymbols() {
        List<StandardReelItem> symbols = List.of(
                StandardReelItem.WLD,
                StandardReelItem.A,
                StandardReelItem.SCT
        );
        HintResult result = new HintResult(symbols, 0, List.of(3, 7, 4));

        when(hintHandler.hint(symbols, 0)).thenReturn(result);

        command.execute(new String[]{"WLD,", "A,", "SCT", "line", "0"});

        verify(hintHandler).hint(symbols, 0);
        verify(renderer).printHintResult(result);
        verify(renderer, never()).printError(anyString());
    }
}
