package ru.netology.nmedia.work

import androidx.work.DelegatingWorkerFactory
import ru.netology.nmedia.repository.PostRepository

class WorkerFactoryDelegate(
    repository: PostRepository
) : DelegatingWorkerFactory() {

    init {
        addFactory(RefreshPostFactory(repository))
        addFactory(SavePostFactory(repository))
        addFactory(RemovePostFactory(repository))
    }
}