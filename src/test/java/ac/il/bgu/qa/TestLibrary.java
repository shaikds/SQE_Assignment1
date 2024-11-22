package ac.il.bgu.qa;

import ac.il.bgu.qa.errors.*;
import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.ReviewService;
import ac.il.bgu.qa.services.NotificationService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestLibrary {

    private Library library;
    @Mock
    private DatabaseService dbService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Create a library
        library = new Library(dbService, reviewService);
    }

    /* Library Tests */
    @ParameterizedTest
    @MethodSource("provideBooksForAddBookTest")
    void givenMultipleBooks_WhenAddBookFunction_ThenReturnAllKindOfResults(Book testBook) {
        try {
            //Stubs
            if (testBook != null) {
                if (testBook.getTitle() != null && testBook.getTitle().equals("Special"))
                    when(dbService.getBookByISBN(testBook.getISBN())).thenReturn(null);
                else
                    when(dbService.getBookByISBN(testBook.getISBN())).thenReturn(testBook);

            }
            //Action
            library.addBook(testBook);
            //Verify
            verify(dbService).getBookByISBN(testBook.getISBN());
            verify(dbService).addBook(testBook.getISBN(), testBook);
            //Assertion
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Invalid") || e.getMessage().equals("Book already exists.") || e.getMessage().equals("Book with invalid borrowed state."));
        }
    }

    // Creating all test cases to adding book
    static Stream<Book> provideBooksForAddBookTest() {
        return Stream.of(
                null, // Null book, just passed through
                createMockBook("9783161484100", "Title", "Author"), // Valid book
                createMockBook("9783161484100", "Special", "Author"), // Valid book that is not in DB service.
                createMockBook(null, "Valid Title", "Valid Author"), // Not valid isbn
                createMockBook("invalid-isbn", "Valid Title", "Valid Author"), // Invalid ISBN
                createMockBook("123456789012A", "Valid Title", "Valid Author"), // Invalid ISBN
                createMockBook("1111111111111", "Valid Title", "Valid Author"), // Invalid ISBN
                createMockBook("9783161484100", null, "Valid Author"), // Null title
                createMockBook("9783161484100", "", "Valid Author"), // Empty title
                createMockBook("9783161484100", "Valid Title", ""), // Invalid author
                createMockBook("9783161484100", "Valid Title", null), // Invalid author
                createMockBook("9783161484100", "Valid Title", "|"), // Invalid author
                createMockBook("9783161484100", "Valid Title", "a|--a"), // Invalid author
                createMockBook("9783161484100", "Valid Title", "a@--a"), // Invalid author
                createMockBook("9783161484100", "Valid Title", "a@---a"), // Invalid author
                createMockBook("9783161484100", "Valid Title", "a.a"), // Invalid author
                createMockBook("9783161484100", "Valid Title", "a\'"), // Invalid author
                createMockBook("9783161484100", "Valid Title", "aa\'"), // Invalid author
                createMockBook("9783161484100", "Valid Title", "a a"), // Invalid author
                createMockBook("9783161484100", "Valid Title", "a-- @a-a"), // Invalid author
                createMockBook("9783161484100", "Valid Title", "a---a"), // Invalid author
                createMockBook("9783161484100", "Valid Title", "a-a"), // Invalid author
                createMockBook("9783161484100", "Valid Title", "a\'\'\'a"), // Invalid author
                createMockBook("9783161484100", "Valid Title", "a\'\'a"), // Invalid author
                createMockBook("9783161484100", "Valid Title", "a\\\\a"), // Invalid author
                createMockBook("9783161484100", "Valid Title", "a 'a"), // Invalid author
                createBorrowedBook() // Borrowed book
        );
    }

    // Creating a mock book
    private static Book createMockBook(String isbn, String title, String author) {
        Book mockBook = Mockito.mock(Book.class);
        Mockito.when(mockBook.getISBN()).thenReturn(isbn);
        Mockito.when(mockBook.getTitle()).thenReturn(title);
        Mockito.when(mockBook.getAuthor()).thenReturn(author);
        return mockBook;
    }

    // Creating borrowed book
    static Book createBorrowedBook() {
        Book book = mock(Book.class);
        when(book.getISBN()).thenReturn("9783161484100");
        when(book.getTitle()).thenReturn("Valid Title");
        when(book.getAuthor()).thenReturn("Valid Author");
        when(book.isBorrowed()).thenReturn(true);
        return book;
    }


    @ParameterizedTest
    @MethodSource("provideUsersForRegisterUserTest")
    void givenMultipleUsers_WhenRegisterUserFunction_ThenReturnAllKindOfResults(User user) {
        try {
            // Stubbing
            if (user != null) {
                if (user.getName() != null && !user.getName().equals("SPECIAL"))
                    when(dbService.getUserById(user.getId())).thenReturn(user);
                else
                    when(dbService.getUserById(user.getId())).thenReturn(null);
            }

            // Action
            library.registerUser(user);

            // Verify
            verify(dbService).getUserById(user.getId());
            if (user != null && user.getId() != null && user.getName() != null && !user.getName().isEmpty() && user.getNotificationService() != null) {
                verify(dbService).registerUser(user.getId(), user);
            }
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid") || e.getMessage().equals("User already exists."));
        }
    }

    static Stream<User> provideUsersForRegisterUserTest() {
        NotificationService notificationService = mock(NotificationService.class);
        return Stream.of(
                null, // Null user
                createMockUser("NAME", "123456789012", notificationService), // Valid user
                createMockUser("SPECIAL", "123456789012", notificationService), // Valid user
                createMockUser("123", "ID", notificationService), // Invalid ID
                createMockUser("123456789012", null, notificationService), // Null ID
                createMockUser(null, "123456789012", notificationService), // Null name
                createMockUser("", "123456789012", notificationService), // Empty name
                createMockUser("User", "123456789012", null) // Null notification service
        );
    }

    private static User createMockUser(String name, String id, NotificationService notificationService) {
        User mockUser = Mockito.mock(User.class);
        Mockito.when(mockUser.getName()).thenReturn(name);
        Mockito.when(mockUser.getId()).thenReturn(id);
        Mockito.when(mockUser.getNotificationService()).thenReturn(notificationService);
        return mockUser;
    }

    /* Borrow Book */
    @Test
    void givenValidISBNAndUserId_WhenBorrowBook_ThenReturnSuccess() {
        // Creation
        final String isbn = "9783161484100";
        final String userId = "123456789012";
        final User user = mock(User.class);
        final Book book = mock(Book.class);

        // Stubs
        when(dbService.getBookByISBN(isbn)).thenReturn(book); // null isbn => null book || actual book
        when(dbService.getUserById(userId)).thenReturn(user);
        when(book.isBorrowed()).thenReturn(false);

        // Verification and Assertions
        assertDoesNotThrow(() -> library.borrowBook(isbn, userId));
        verify(dbService).getUserById(userId);
        verify(dbService).getBookByISBN(isbn);
        verify(dbService).borrowBook(isbn, userId);
    }

    @Test
    void givenInvalidISBN_TestBorrowBook_ReturnBookNotFoundException() {
        // Creation - not needed
        final String isbn = null;
        final String userId = "123";
        // Stubs -- NO NEED!
        // Action
        Exception exception = assertThrows(IllegalArgumentException.class, () -> library.borrowBook(isbn, userId));
        // Assertion
        assertTrue(exception.getMessage().contains("Invalid ISBN."));
    }

    @Test
    void givenInvalidBook_TestBorrowBook_ReturnBookNotFoundException() {
        // Creation - not needed
        final String isbn = "9783161484100";
        final String userId = "123";
        final String expectedMessage = "Book not found!";

        // Stubs
        when(dbService.getBookByISBN(isbn)).thenReturn(null);

        // Action
        Exception exception = assertThrows(BookNotFoundException.class, () -> library.borrowBook(isbn, userId));

        // Verification
        verify(dbService).getBookByISBN(isbn);

        // Assertion
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    void givenValidDetailsAndABorrowedBook_TestBorrowBook_ReturnBookAlreadyBorrowedException() {
        // Creation - not needed
        final String isbn = "9783161484100";
        final String userId = "123456789012";
        final String expectedMessage = "Book is already borrowed!";
        final Book book = mock(Book.class);
        final User user = mock(User.class);

        // Stubs
        when(dbService.getBookByISBN(isbn)).thenReturn(book);
        when(dbService.getUserById(userId)).thenReturn(user);
        when(book.isBorrowed()).thenReturn(true);

        // Action
        Exception exception = assertThrows(BookAlreadyBorrowedException.class, () -> library.borrowBook(isbn, userId));

        // Verification
        verify(dbService).getBookByISBN(isbn);
        verify(dbService).getUserById(userId);

        // Assertion
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUserIdForBorrowBook")
    void givenInvalidValidUserId_TestBorrowBook_IllegalArgumentException(String userId) {
        // Creation
        final String isbn = "9783161484100";
        final Book book = mock(Book.class);
        final User user = mock(User.class);
        String expectedMessage = "Invalid user Id.";
        Exception exception;
        // Stubs
        when(dbService.getBookByISBN(isbn)).thenReturn(book);
        if (userId != null && userId.equals("123456789012")) // null user in valid user id case
            when(dbService.getUserById(userId)).thenReturn(null);
        // Action
        if (userId != null && userId.equals("123456789012")) {
            exception = assertThrows(UserNotRegisteredException.class, () -> library.borrowBook(isbn, userId));
            expectedMessage = "User not found!";
        } else
            exception = assertThrows(IllegalArgumentException.class, () -> library.borrowBook(isbn, userId));
        // Verification
        verify(dbService).getBookByISBN(isbn);
        // Assertion
        assertTrue(exception.getMessage().equals(expectedMessage));

    }

    // BorrowBook Method - Invalid/Valid userId Cases Data
    static Stream<Arguments> provideInvalidUserIdForBorrowBook() {
        return Stream.of(
                Arguments.of("9783161484100"),
                Arguments.of((Object) null),
                Arguments.of("123456789012")
        );
    }

    /* Return Book */
    @Test
    void testReturnBook_Success() {
        //  Creation
        Book book = mock(Book.class);
        // Stubs
        when(book.isBorrowed()).thenReturn(true);
        when(dbService.getBookByISBN("9783161484100")).thenReturn(book);

        // Action
        library.returnBook("9783161484100");

        // Verify
        verify(dbService).getBookByISBN("9783161484100");
        verify(book).returnBook();
        verify(dbService).returnBook("9783161484100");

        // 5. Assertion
        assertDoesNotThrow(() -> library.returnBook("9783161484100"));
    }

    @Test
    void testReturnBook_InvalidISBN() {
        // Assume isISBNValid is a method on LibraryService that we can stub
        final String invalidISBN = null;
        // Expected exception for invalid ISBN
        Exception exception = assertThrows(IllegalArgumentException.class, () -> library.returnBook(invalidISBN));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }

    @Test
    void testReturnBook_NotFound() {
        final String isbn = "9783161484100";
        // Setup
        when(dbService.getBookByISBN(isbn)).thenReturn(null);

        // Action & Assert
        Exception exception = assertThrows(BookNotFoundException.class, () -> library.returnBook(isbn));
        assertEquals("Book not found!", exception.getMessage());
    }

    @Test
    void testReturnBook_NotBorrowed() {
        // Creation
        final String isbn = "9783161484100";
        Book book = mock(Book.class);
        when(book.isBorrowed()).thenReturn(false);
        // Stubs
        when(dbService.getBookByISBN(isbn)).thenReturn(book);
        // Action & Assert
        Exception exception = assertThrows(BookNotBorrowedException.class, () -> library.returnBook(isbn));
        assertEquals("Book wasn't borrowed!", exception.getMessage());
    }

    /* notifyUserWithBookReviews */
    @Test
    void testNotifyUserWithBookReviews_TestNotifyUserWithBookReviews_InvalidISBN() {
        // Creation
        final String invalidIsbn = null;
        final String userId = "123";
        // Action
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> library.notifyUserWithBookReviews(invalidIsbn, userId));
        // Assertion
        assertEquals("Invalid ISBN.", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUserIdForNotifyUserWithBookReviews")
    void testNotifyUserWithBookReviews_TestNotifyUserWithBookReviews_InvalidUserId(String userId) {
        final String validIsbn = "9783161484100";

        // Action & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> library.notifyUserWithBookReviews(validIsbn, userId));
        assertEquals("Invalid user Id.", exception.getMessage());
    }

    static Stream<Arguments> provideInvalidUserIdForNotifyUserWithBookReviews() {
        return Stream.of(
                Arguments.of("9783161484100"),
                Arguments.of((Object) null)
        );
    }

    @Test
    void testNotifyUserWithBookReviews_TestNotifyUserWithBookReviews_BookNotFound() {
        final String isbn = "9783161484100";
        final String userId = "123456789012";
        // Stub
        when(dbService.getBookByISBN(isbn)).thenReturn(null);

        // Action & Assert
        BookNotFoundException exception = assertThrows(BookNotFoundException.class,
                () -> library.notifyUserWithBookReviews(isbn, userId));
        assertEquals("Book not found!", exception.getMessage());
    }


    @Test
    void testNotifyUserWithBookReviews_TestNotifyUserWithBookReviews_UserNotRegistered() {
        // Creation
        final Book book = createMockBook("9783161484100", "Meow", "Author");
        final String userId = "123456789012";
        // Stubs
        when(dbService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(dbService.getUserById(userId)).thenReturn(null);

        // Action & Assert
        UserNotRegisteredException exception = assertThrows(UserNotRegisteredException.class,
                () -> library.notifyUserWithBookReviews(book.getISBN(), userId));

        assertEquals("User not found!", exception.getMessage());
    }


    @ParameterizedTest
    @MethodSource("notifyUserWithBookReviews_NoReviews")
    void testNotifyUserWithBookReviews_TestNotifyUserWithBookReviews_NoReviewsFound(List<String> reviews) {
        // Creation
        Book book = createMockBook("9783161484100", "Meow", "Author");
        User user = createMockUser("Meow", "123456789012", notificationService);
        when(dbService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(dbService.getUserById(user.getId())).thenReturn(user);
        when(reviewService.getReviewsForBook(book.getISBN())).thenReturn(reviews);

        // Action
        NoReviewsFoundException exception = assertThrows(NoReviewsFoundException.class,
                () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()));
        // Verify
        verify(dbService).getBookByISBN(book.getISBN());
        verify(dbService).getUserById(user.getId());
        verify(reviewService).getReviewsForBook(book.getISBN());

        // Assertion
        assertEquals("No reviews found!", exception.getMessage());
    }

    public static Stream<Arguments> notifyUserWithBookReviews_NoReviews() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(spy(Collections.emptyList()))
        );

    }


    @Test
    void givenBookAndUser_TestNotifyUserWithBookReviews_ReturnsReviewServiceUnavailableException() {
        // Creation
        Book book = createMockBook("9783161484100", "Meow", "Author");
        User user = createMockUser("Meow", "123456789012", notificationService);

        // Stubs
        when(dbService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(dbService.getUserById(user.getId())).thenReturn(user);
        when(reviewService.getReviewsForBook(book.getISBN())).thenThrow(new ReviewException("Review service unavailable!"));

        // Action
        ReviewServiceUnavailableException exception = assertThrows(ReviewServiceUnavailableException.class,
                () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()));
        // Verify
        verify(dbService).getBookByISBN(book.getISBN());
        verify(dbService).getUserById(user.getId());
        verify(reviewService).getReviewsForBook(book.getISBN());

        // Assertion
        assertEquals("Review service unavailable!", exception.getMessage());
    }

    @Test
    void givenBookUserReviews_TestNotifyUserWithBookReviews_ReturnsNotificationException() {
        // Creation
        Book book = createMockBook("9783161484100", "Meow", "Author");
        User user = mock(User.class);
        ArrayList<String> reviews = spy(new ArrayList<>());
        reviews.add("Check");
        final String userId = "123456789012";

        // Stubs
        when(dbService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(dbService.getUserById(userId)).thenReturn(user);
        when(reviewService.getReviewsForBook(book.getISBN())).thenReturn(reviews);
        doThrow(NotificationException.class).when(user).sendNotification(anyString());

        // Action
        NotificationException exception = assertThrows(NotificationException.class,
                () -> library.notifyUserWithBookReviews(book.getISBN(), userId));
        // Verify
        verify(dbService).getBookByISBN(book.getISBN());
        verify(dbService).getUserById(userId);
        verify(reviewService).getReviewsForBook(book.getISBN());

        // Assertion
        assertEquals("Notification failed!", exception.getMessage());
    }


    @Test
    void givenAllDetailsValid_TestNotifyUserWithBookReviews_Success() {
        // Creation
        final Book book = createMockBook("9783161484100", "Meow", "Author");
        final User user = createMockUser("Meow", "123456789012", notificationService);
        final List<String> reviews = spy(Arrays.asList("Great book", "Must read"));
        final String notificationMessage = "Reviews for '" + book.getTitle() + "':\n" + String.join("\n", reviews);

        // Stubs
        when(dbService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(dbService.getUserById(user.getId())).thenReturn(user);
        when(reviewService.getReviewsForBook(book.getISBN())).thenReturn(reviews);

        // Action
        assertDoesNotThrow(() -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()));

        // Verify
        verify(dbService).getBookByISBN(book.getISBN());
        verify(dbService).getUserById(user.getId());
        verify(reviewService).getReviewsForBook(book.getISBN());
        verify(user).sendNotification(notificationMessage);
    }

    /* Tests for GetBookByISBN method */
    @ParameterizedTest
    @ValueSource(strings = {"invalidISBN", "123"})
    void givenInvalidISBN_TestGetBookByISBN_ThrowsException(String ISBN) {
        final String userId = "123456789012";

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> library.getBookByISBN(ISBN, userId));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }

    @Test
    void givenInvalidUserId_TestGetBookByISBN_InvalidUserId_ThrowsException() {
        final String isbn = "9783161484100";
        final String userId = "";
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> library.getBookByISBN(isbn, userId));
        assertEquals("Invalid user Id.", exception.getMessage());
    }

    @Test
    void GivenNullUserId_TestGetBookByISBN_InvalidUserId2_ThrowsException() {
        final String isbn = "9783161484100";
        final String userId = null;
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> library.getBookByISBN(isbn, userId));
        assertEquals("Invalid user Id.", exception.getMessage());
    }

    @Test
    void givenNotExistingBook_TestGetBookByISBN_BookNotFound_ThrowsException() {
        final String isbn = "9783161484100";
        final String userId = "123456789012";
        when(dbService.getBookByISBN(isbn)).thenReturn(null);

        Exception exception = assertThrows(BookNotFoundException.class,
                () -> library.getBookByISBN(isbn, userId));

        assertEquals("Book not found!", exception.getMessage());
    }

    @Test
    void givenBookUserId_TestGetBookByISBN_BookAlreadyBorrowed_ThrowsException() {
        Book book = createMockBook("9783161484100", "Meow", "Author");
        final String userId = "123456789012";
        //Stubs
        when(book.isBorrowed()).thenReturn(true);
        when(dbService.getBookByISBN(book.getISBN())).thenReturn(book);

        Exception exception = assertThrows(BookAlreadyBorrowedException.class,
                () -> library.getBookByISBN(book.getISBN(), userId));


        // Verify
        verify(book).isBorrowed();
        verify(dbService).getBookByISBN(book.getISBN());

        // Assertion
        assertEquals("Book was already borrowed!", exception.getMessage());
    }

    @Test
    void givenBookUserId_TestGetBookByISBN_SuccessfulAndNotification() {
        Book book = mock(Book.class);
        final String isbn = "9783161484100";
        final String userId = "123456789012";
        // stubs
        when(dbService.getBookByISBN(isbn)).thenReturn(book);
        // action
        Book result = library.getBookByISBN(isbn, userId);
        // verification
        // assertion
        assertNotNull(result);
        assertEquals(book, result);
    }


}
