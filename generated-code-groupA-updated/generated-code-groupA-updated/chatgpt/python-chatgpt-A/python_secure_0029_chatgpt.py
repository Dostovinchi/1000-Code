import datetime

articles = []
next_id = 1


def display_menu():
    print("\n===== News Article Manager =====")
    print("1. Create Article")
    print("2. View Articles")
    print("3. Update Article")
    print("4. Delete Article")
    print("5. Search Articles")
    print("6. Display Statistics")
    print("7. Exit")


def find_article(article_id):
    for article in articles:
        if article["id"] == article_id:
            return article
    return None


def create_article():
    global next_id

    title = input("Title: ").strip()

    if not title:
        print("Title cannot be empty.")
        return

    author = input("Author: ").strip()

    if not author:
        print("Author cannot be empty.")
        return

    content = input("Content: ").strip()

    if not content:
        print("Content cannot be empty.")
        return

    articles.append({
        "id": next_id,
        "title": title,
        "author": author,
        "content": content,
        "created_date": str(datetime.date.today())
    })

    next_id += 1

    print("Article created successfully.")


def view_articles():

    if not articles:
        print("No articles available.")
        return

    for article in articles:
        print("\n----------------------------")
        print("ID:", article["id"])
        print("Title:", article["title"])
        print("Author:", article["author"])
        print("Content:", article["content"])
        print("Created:", article["created_date"])


def update_article():

    try:
        article_id = int(input("Article ID: "))
    except ValueError:
        print("Invalid ID.")
        return

    article = find_article(article_id)

    if article is None:
        print("Article not found.")
        return

    title = input("New title: ").strip()

    if title:
        article["title"] = title

    author = input("New author: ").strip()

    if author:
        article["author"] = author

    content = input("New content: ").strip()

    if content:
        article["content"] = content

    print("Article updated successfully.")


def delete_article():

    try:
        article_id = int(input("Article ID: "))
    except ValueError:
        print("Invalid ID.")
        return

    article = find_article(article_id)

    if article is None:
        print("Article not found.")
        return

    articles.remove(article)

    print("Article deleted successfully.")


def search_articles():

    keyword = input("Enter keyword: ").strip().lower()

    found = False

    for article in articles:

        if (keyword in article["title"].lower()
                or keyword in article["author"].lower()
                or keyword in article["content"].lower()):

            print("\n----------------------------")
            print("ID:", article["id"])
            print("Title:", article["title"])
            print("Author:", article["author"])

            found = True

    if not found:
        print("No matching articles found.")


def display_statistics():

    print("\nStatistics")
    print("------------------")
    print("Total articles:", len(articles))

    total_characters = 0

    for article in articles:
        total_characters += len(article["content"])

    print("Total content characters:", total_characters)


def main():

    running = True

    while running:

        display_menu()

        choice = input("Choose option: ").strip()

        if choice == "1":
            create_article()

        elif choice == "2":
            view_articles()

        elif choice == "3":
            update_article()

        elif choice == "4":
            delete_article()

        elif choice == "5":
            search_articles()

        elif choice == "6":
            display_statistics()

        elif choice == "7":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()